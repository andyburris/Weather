package com.andb.apps.weather

import com.andb.apps.weather.data.model.Conditions
import com.andb.apps.weather.data.model.FixedLocation
import com.andb.apps.weather.data.model.SelectedLocation
import com.andb.apps.weather.data.repository.location.LocationRepo
import com.andb.apps.weather.data.repository.weather.WeatherRepo
import com.andb.apps.weather.ui.home.HomeScreenState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private val debugLocation =
    SelectedLocation.Fixed("test", FixedLocation(0.0, 0.0, "Chautauqua", "NY")) //TODO: remove

data class Machine(
    private val weatherRepo: WeatherRepo,
    private val locationRepo: LocationRepo,
    private val hasLocationPermission: Flow<Boolean>,
    private val coroutineScope: CoroutineScope,
) {

    sealed class Action(open val action: suspend Machine.() -> Unit) {
        object UpdateWeather : Action({
            when (val locationState = location.value) {
                is LocationState.WithLocation -> conditions.value = weatherRepo
                    .getForecast(locationState.location.latitude, locationState.location.longitude)
                    .toConditionState()
                is LocationState.NoLocation -> {}
            }
        })

        sealed class SelectLocation(override val action: suspend Machine.() -> Unit) :
            Action(action) {
            object Current : SelectLocation({
                this.selectedLocation.value = SelectedLocation.Current
            })

            data class Fixed(val locationID: String) : SelectLocation({
                this.selectedLocation.value =
                    locationRepo.getLocationByID(locationID) ?: SelectedLocation.Current
            })
        }

        object Refresh : Action({
            when (val locationState = location.value) {
                is LocationState.Fixed -> conditions.value = weatherRepo
                    .getForecast(locationState.location.latitude, locationState.location.longitude)
                    .toConditionState()
                is LocationState.Current.NoAccess -> {
                    location.value = LocationState.Current.NotLoaded
                }
                // if locationstate == fixed, refresh conditions
                // if locationstate == current with permission, refresh current location
                // if locationstate == current without permission, request permission? or do nothing? or throw error since it shouldn't be possible?
            }
        })
    }

    fun handleAction(action: Action) {
        coroutineScope.launch { action.action.invoke(this@Machine) }
    }

    private val selectedLocation = MutableStateFlow<SelectedLocation>(SelectedLocation.Current)
    private val location = selectedLocation.map { selectedLocation ->
        when (selectedLocation) {
            is SelectedLocation.Fixed -> LocationState.Fixed(selectedLocation.fixedLocation)
            SelectedLocation.Current -> locationRepo.getCurrentLocation()
                .map { LocationState.Current.Found(it) }
                .getOrDefault(LocationState.Current.NoPermission)
        }
    }.stateIn(coroutineScope, SharingStarted.Eagerly, LocationState.Current.NotLoaded)

    private val savedLocations = locationRepo.savedLocations()
    private val loadingCurrentLocation = MutableStateFlow(false)
    private val currentLocation =
        MutableStateFlow<LocationState.Current>(LocationState.Current.NotLoaded)
    private val conditions = MutableStateFlow<ConditionState>(ConditionState.Loading)
    val homeScreen =
        location.stateCombine(conditions, coroutineScope) { locationState, conditionState ->
            println("zipping locationState = $locationState, conditionState = $conditionState")
            HomeScreenState(location = locationState, conditionState = conditionState)
        }
}

fun <T1, T2, R> StateFlow<T1>.stateCombine(
    other: StateFlow<T2>,
    coroutineScope: CoroutineScope,
    transform: (T1, T2) -> R
) = this
    .combine(other) { t1, t2 -> transform(t1, t2) }
    .stateIn(coroutineScope, SharingStarted.Eagerly, transform(this.value, other.value))

sealed interface LocationState {
    sealed interface NoLocation : LocationState
    sealed interface WithLocation : LocationState {
        val location: FixedLocation
    }

    sealed class Current(open val isLoading: Boolean) : LocationState {
        object NotLoaded : Current(true), NoLocation
        object NoPermission : Current(false), NoLocation
        data class NoAccess(override val isLoading: Boolean) : Current(isLoading), NoLocation
        data class Found(override val location: FixedLocation, override val isLoading: Boolean) :
            Current(isLoading), WithLocation
    }

    data class Fixed(val id: String, override val location: FixedLocation) : LocationState,
        WithLocation
}

val LocationState.hasLocation get() = this is LocationState.WithLocation

sealed class ConditionState {
    object Loading : ConditionState()
    object Error : ConditionState()
    data class Weather(val data: Conditions) : ConditionState()
}

fun Result<Conditions>.toConditionState() =
    this.map { ConditionState.Weather(it) }.getOrDefault(ConditionState.Error)