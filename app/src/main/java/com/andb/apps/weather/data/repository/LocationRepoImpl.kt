package com.andb.apps.weather.data.repository

import android.annotation.SuppressLint
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.LiveData
import com.andb.apps.weather.data.local.LocationDao
import com.andb.apps.weather.data.local.Prefs
import com.andb.apps.weather.data.model.Location
import com.andb.apps.weather.util.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@ExperimentalCoroutinesApi
class LocationRepoImpl(
    private val locationsDao: LocationDao,
    private val locationProvider: FusedLocationProviderClient,
    private val geocoder: Geocoder,
    private val placesClient: PlacesClient
) : LocationRepo {

    private var selectedID = InitialLiveData(Prefs.selectedID)

    override fun getAllLocations(): Flow<List<Location>> {
        return locationsDao.getLocations()
    }

    @SuppressLint("MissingPermission")
    override fun getSelectedLocation(): LiveData<Location?> {
        return selectedID.mapAsync { selectedID ->
            return@mapAsync when (selectedID) {
                "" -> {
                    val androidLocation =
                        locationProvider.lastLocation.getSync() ?: return@mapAsync null
                    val address = geocoder.getFromLocation(
                        androidLocation.latitude,
                        androidLocation.longitude,
                        1
                    )[0]
                    Log.d("getSelectedLocation", "current location address: $address")
                    Location(
                        "",
                        androidLocation.latitude,
                        androidLocation.longitude,
                        address.locality,
                        address.adminArea
                    )
                }
                else -> getAllLocations().first().find { it.id == selectedID }
            }
        }
    }

    override suspend fun getSuggestionsFromSearch(
        term: String,
        currentLat: Double,
        currentLong: Double
    ): List<AutocompletePrediction> {
        Log.d("getSuggestionFromSearch", "autocompleting $term")
        val autocompleteResults = placesClient.autocompleteWith(term) {
            latLong = Pair(currentLat, currentLat)
            radius = 1.0
        }
        Log.d(
            "getSuggestionFromSearch",
            "autocomplete predictions: ${autocompleteResults?.autocompletePredictions}"
        )
        return autocompleteResults?.autocompletePredictions ?: emptyList()
    }

    override suspend fun getSuggestionsFromSearch(term: String): List<AutocompletePrediction> {
        val currentLocation = locationProvider.lastLocation.getSync()

        val currentLat = currentLocation?.latitude ?: 0.0
        val currentLong = currentLocation?.longitude ?: 0.0
        Log.d("getSuggestionFromSearch", "currentLocation: $currentLat, $currentLong")
        return getSuggestionsFromSearch(term, currentLat, currentLong)
    }

    override suspend fun getLocationByID(id: String): Location? {
        val place = placesClient.getPlaceByID(
            id,
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS_COMPONENTS
            )
        )?.place ?: return null
        Log.d("getLocationByID", "addressComponents: ${place.addressComponents}")
        val addressComponents = place.addressComponents!!.asList()
        val region: String = when {
            addressComponents.any { it.types.contains("administrative_area_level_1") } -> {
                val component =
                    addressComponents.last { component -> component.types.any { it.contains("administrative_area_level_1") } }
                component.shortName ?: component.name
            }
            else -> addressComponents.last().shortName ?: addressComponents.last().name
        }
        return Location(place.id!!, place.latLng!!, place.name!!, region)
    }

    override suspend fun saveLocation(location: Location) {
        locationsDao.saveLocation(location)
    }

    override suspend fun deleteLocation(location: Location) {
        locationsDao.removeLocation(location)
    }

    override fun selectLocation(id: String) {
        selectedID.postValue(id)
    }

    override fun refresh() {
        selectedID.postValue(selectedID.value)
    }
}