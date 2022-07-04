package com.andb.apps.weather.data.repository.location

import android.location.Geocoder
import android.location.Location
import android.util.Log
import com.andb.apps.weather.data.local.LocationDao
import com.andb.apps.weather.data.local.Prefs
import com.andb.apps.weather.data.model.FixedLocation
import com.andb.apps.weather.data.model.SavedLocation
import com.andb.apps.weather.util.autocompleteWith
import com.andb.apps.weather.util.getPlaceByID
import com.andb.apps.weather.util.getSync
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@ExperimentalCoroutinesApi
class LocationRepoImpl(
    private val locationsDao: LocationDao,
    private val locationProvider: FusedLocationProviderClient,
    private val geocoder: Geocoder,
    private val placesClient: PlacesClient
) : LocationRepo {

    private var selectedID = MutableStateFlow(Prefs.selectedID)

    override suspend fun getCurrentLocation(): Flow<Result<FixedLocation>> {
        locationProvider.requestLocationUpdates(LocationRequest(), object : LocationCallback {

        })
        runCatching {
            val currentLocation = try {
                locationProvider.lastLocation.getSync()
            } catch (e: SecurityException) {
                throw Error("Don't have location permission")
            }
            currentLocation?.toFixedLocation() ?: throw Error("Could not get current location")
        }
    }

    override fun savedLocations(): Flow<List<SavedLocation>> {
        return locationsDao.getLocations()
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
        val currentLocation = try {
            locationProvider.lastLocation.getSync()
        } catch (e: SecurityException) {
            null
        }

        val currentLat = currentLocation?.latitude ?: 0.0
        val currentLong = currentLocation?.longitude ?: 0.0
        Log.d("getSuggestionFromSearch", "currentLocation: $currentLat, $currentLong")
        return getSuggestionsFromSearch(term, currentLat, currentLong)
    }

    override suspend fun getLocationByID(id: String): SavedLocation? {
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
        return SavedLocation(place.id!!, place.latLng!!, place.name!!, region)
    }

    override suspend fun saveLocation(location: SavedLocation) {
        locationsDao.saveLocation(location)
    }

    override suspend fun deleteLocation(location: SavedLocation) {
        locationsDao.removeLocation(location)
    }

    fun Location.toFixedLocation(): FixedLocation {
        val address = geocoder.getFromLocation(this.latitude, this.longitude, 1)[0]
        Log.d("getSelectedLocation", "current location address: $address")
        return FixedLocation(
            this.latitude,
            this.longitude,
            address.locality,
            address.adminArea
        )
    }
}