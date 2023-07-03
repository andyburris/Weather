package com.andb.apps.weather

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.andb.apps.weather.data.model.Conditions
import com.andb.apps.weather.data.model.FixedLocation
import com.andb.apps.weather.data.model.SelectedLocation
import com.andb.apps.weather.data.repository.location.LocationRepo
import com.andb.apps.weather.data.repository.weather.WeatherRepo
import com.andb.apps.weather.ui.home.HomeScreenState
import com.andb.apps.weather.ui.location.LocationPickerState
import com.andb.apps.weather.ui.location.LocationSearchState
import com.andb.apps.weather.util.combine6
import com.google.android.gms.tasks.RuntimeExecutionException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class Machine(
    private val weatherRepo: WeatherRepo,
    private val locationRepo: LocationRepo,
    private val hasLocationPermission: Flow<Boolean>,
    private val coroutineScope: CoroutineScope,
    private val onRequestLocationPermission: () -> Unit
) {
    init {
        coroutineScope.launch {
            currentLocation.value = locationRepo.getCurrentLocation().toCurrentLocationState()
        }
    }

    private val savedLocations =
        locationRepo.savedLocations().stateIn(coroutineScope, SharingStarted.Eagerly, listOf())
    private val currentLocation =
        MutableStateFlow<LocationState.Current>(LocationState.Current.NotLoaded)
    private val selectedLocation = MutableStateFlow<SelectedLocation>(SelectedLocation.Current)
    private val location = combine(
        selectedLocation,
        currentLocation,
        savedLocations,
    ) { selectedLocation, currentLocation, savedLocations ->
        val newSelectedLocation = when (selectedLocation) {
            is SelectedLocation.Fixed -> when (val foundLocation =
                savedLocations.find { it.id == selectedLocation.id }) {
                null -> currentLocation
                else -> foundLocation
            }

            SelectedLocation.Current -> currentLocation
        }
        if (newSelectedLocation is LocationState.WithLocation) this.handleAction(Action.UpdateWeather)
        return@combine newSelectedLocation
    }.stateIn(coroutineScope, SharingStarted.Eagerly, LocationState.Current.NotLoaded)

    private val searchTerm = MutableStateFlow("")
    private val searchResults = searchTerm.debounce(300.milliseconds).map { term ->
        if (term.isBlank()) return@map listOf()
        val autocompleteResults = locationRepo.getSuggestionsFromSearch(term)
        val jobs = autocompleteResults.map {
            CoroutineScope(Dispatchers.IO).async {
                locationRepo.getLocationByID(it.placeId).getOrNull()
            }
        }
        val results = jobs.mapNotNull { it.await() }
        results
    }.stateIn(coroutineScope, SharingStarted.Eagerly, listOf())

    private val conditions = MutableStateFlow<ConditionState>(ConditionState.NotLoaded)

    val homeScreenState: StateFlow<HomeScreenState> = combine6(
        location,
        conditions,
        currentLocation,
        savedLocations,
        searchTerm,
        searchResults,
    ) { locationState, conditionState, currentLocation, savedLocations, searchTerm, searchResults ->
        println("zipping locationState = $locationState, conditionState = $conditionState")
        HomeScreenState(
            selectedLocation = locationState,
            locationPickerState = LocationPickerState(
                currentLocation = currentLocation,
                savedLocations = savedLocations,
                searchState = LocationSearchState(
                    searchTerm,
                    searchResults,
                )
            ),
            conditionState = conditionState,
        )
    }.stateIn(
        coroutineScope,
        SharingStarted.Eagerly,
        HomeScreenState(
            selectedLocation = LocationState.Current.NotLoaded,
            locationPickerState = LocationPickerState(
                LocationState.Current.NotLoaded,
                emptyList(),
                LocationSearchState("", listOf()),
            ),
            conditionState = ConditionState.NotLoaded,
        )
    )

    private val selectedScreen: MutableStateFlow<Screen> = MutableStateFlow(Screen.Home)
    val currentScreenState: StateFlow<ScreenState> =
        selectedScreen.combine(homeScreenState) { screen, homeScreenState ->
            when (screen) {
                Screen.Home -> homeScreenState
                else -> SettingsState
            }
        }.stateIn(coroutineScope, SharingStarted.Eagerly, homeScreenState.value)

    fun handleAction(action: Action) {
        coroutineScope.launch { action.action.invoke(this@Machine) }
    }

    sealed class Action(open val action: suspend Machine.() -> Unit) {
        data class OpenScreen(val screen: Screen) : Action({
            this.selectedScreen.value = screen
        })

        object UpdateWeather : Action({
            when (val locationState = location.value) {
                is LocationState.WithLocation -> conditions.value = weatherRepo
                    .getForecast(locationState.location.latitude, locationState.location.longitude)
                    .toConditionState()

                is LocationState.NoLocation -> throw Error("Shouldn't be able to update weather if no location")
            }
        })

        data class SearchLocation(val term: String) : Action({
            println("updating search term to $term")
            searchTerm.value = term
        })

        data class DeleteSavedLocation(val location: LocationState.Fixed) : Action({
            val currentlySelected = selectedLocation.value
            if (currentlySelected is SelectedLocation.Fixed && currentlySelected.id == location.id) {
                selectedLocation.value = SelectedLocation.Current
            }
            CoroutineScope(Dispatchers.IO).launch {
                locationRepo.deleteLocation(location)
            }
        })

        sealed class SelectLocation(override val action: suspend Machine.() -> Unit) :
            Action(action) {
            object Current : SelectLocation({
                this.selectedLocation.value = SelectedLocation.Current
            })

            data class Fixed(val locationID: String) : SelectLocation({
                CoroutineScope(Dispatchers.IO).launch {
                    if (savedLocations.value.none { it.id == locationID }) {
                        val foundLocation = locationRepo.getLocationByID(locationID).getOrNull()
                            ?: throw Error("location should always be found")
                        locationRepo.saveLocation(foundLocation)
                    }
                    selectedLocation.value = SelectedLocation.Fixed(locationID)
                        ?: SelectedLocation.Current //TODO: work on double refresh that happens on delete

                    searchTerm.value = ""
                }
            })
        }

        sealed class CurrentLocation(override val action: suspend Machine.() -> Unit) :
            Action(action) {
            object RequestPermission : CurrentLocation({
                onRequestLocationPermission.invoke()
            })

            object Refresh : CurrentLocation({
                currentLocation.value = when (val currentVal = currentLocation.value) {
                    LocationState.Current.NotLoaded -> throw Error("Should not be able to refresh NotLoaded")
                    is LocationState.Current.Error -> currentVal.copy(isLoading = true)
                    is LocationState.Current.Ok -> currentVal.copy(isLoading = true)
                }
                val newLocationState = locationRepo.getCurrentLocation().toCurrentLocationState()
                currentLocation.value = newLocationState
            })
        }
    }
}

sealed interface LocationState {
    sealed interface NoLocation : LocationState
    sealed interface WithLocation : LocationState {
        val location: FixedLocation
    }

    sealed class Current : LocationState {
        abstract val isLoading: Boolean

        object NotLoaded : Current(), NoLocation {
            override val isLoading: Boolean = true
        }

        data class Error(val error: CurrentLocationError, override val isLoading: Boolean) :
            Current(), NoLocation

        data class Ok(override val location: FixedLocation, override val isLoading: Boolean) :
            Current(), WithLocation
    }

    @Entity
    data class Fixed(
        @PrimaryKey val id: String,
        override val location: FixedLocation,
    ) : LocationState, WithLocation
}


sealed class ConditionState {
    abstract val isLoading: Boolean

    object NotLoaded : ConditionState() {
        override val isLoading: Boolean = true
    }

    data class Error(val error: ConditionError, override val isLoading: Boolean) : ConditionState()
    data class Ok(val resource: Conditions, override val isLoading: Boolean) : ConditionState()
}

sealed class CurrentLocationError {
    object NoPermission : CurrentLocationError()
    object NoAccess : CurrentLocationError()
}

sealed class ConditionError {
    object NoInternet : ConditionError()
}

fun Result<Conditions>.toConditionState(): ConditionState = this
    .map { ConditionState.Ok(it, false) }
    .recover { ConditionState.Error(ConditionError.NoInternet, false) }
    .getOrThrow()

fun Result<FixedLocation>.toCurrentLocationState(): LocationState.Current = this
    .map { LocationState.Current.Ok(it, false) }
    .recover {
        println("recovering, throwable = $it")
        when (it) {
            is RuntimeExecutionException, is SecurityException -> LocationState.Current.Error(
                CurrentLocationError.NoPermission,
                false
            )

            else -> LocationState.Current.Error(CurrentLocationError.NoAccess, false)
        }
    }.getOrThrow()

enum class Screen {
    Home, Settings, Test
}

interface ScreenState

object SettingsState : ScreenState