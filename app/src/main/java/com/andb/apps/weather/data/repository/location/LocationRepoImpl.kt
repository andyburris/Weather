package com.andb.apps.weather.data.repository.location

import android.location.Geocoder
import android.location.Location
import android.util.Log
import com.andb.apps.weather.LocationState
import com.andb.apps.weather.data.local.LocationDao
import com.andb.apps.weather.data.model.FixedLocation
import com.andb.apps.weather.data.model.toFixedLocationState
import com.andb.apps.weather.data.model.toSavedLocation
import com.andb.apps.weather.util.autocompleteWith
import com.andb.apps.weather.util.getPlaceByID
import com.andb.apps.weather.util.getSync
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@ExperimentalCoroutinesApi
class LocationRepoImpl(
    private val locationsDao: LocationDao,
    private val locationProvider: FusedLocationProviderClient,
    private val geocoder: Geocoder,
    private val placesClient: PlacesClient
) : LocationRepo {

    override suspend fun getCurrentLocation(): Result<FixedLocation> = runCatching {
        return@runCatching try {
            locationProvider.lastLocation
        } catch (e: SecurityException) {
            throw e
        } //since exceptions thrown in suspendCoroutine aren't caught by runCatching, maybe this will work?
    }.mapCatching { lastLocation ->
        lastLocation.getSync()
            .map { it?.toFixedLocation() ?: throw Error("Could not get current location") }
            .getOrThrow()
    }

    override fun savedLocations(): Flow<List<LocationState.Fixed>> = locationsDao
        .getLocations()
        .map { list -> list.map { it.toFixedLocationState() } }


    override suspend fun getSuggestionsFromSearch(term: String): List<AutocompletePrediction> {
        Log.d("getSuggestionFromSearch", "autocompleting $term")
        val currentLocation = getCurrentLocation().getOrNull() ?: FixedLocation(0.0, 0.0, "", "")
        return placesClient
            .autocompleteWith(term) {
                latLong = Pair(currentLocation.latitude, currentLocation.longitude)
                radius = 1.0
            }
            .map { it?.autocompletePredictions }
            .getOrNull() ?: emptyList()
    }

    override suspend fun getLocationByID(id: String): Result<LocationState.Fixed> =
        placesClient.getPlaceByID(
            id,
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS_COMPONENTS
            )
        ).mapCatching { placeResponse ->
            val place = placeResponse?.place ?: throw IllegalStateException("Place not found")
            val addressComponents = place.addressComponents!!.asList()
            val region: String = when {
                addressComponents.any { it.types.contains("administrative_area_level_1") } -> {
                    val component =
                        addressComponents.last { component -> component.types.any { it.contains("administrative_area_level_1") } }
                    component.shortName ?: component.name
                }

                else -> addressComponents.last().shortName ?: addressComponents.last().name
            }
            LocationState.Fixed(place.id!!, FixedLocation(place.latLng!!, place.name!!, region))
        }

    override suspend fun saveLocation(location: LocationState.Fixed) = locationsDao
        .saveLocation(location.toSavedLocation())

    override suspend fun deleteLocation(location: LocationState.Fixed) = locationsDao
        .removeLocation(location.toSavedLocation())

    suspend fun Location.toFixedLocation(): FixedLocation? {
        val address =
            geocoder.getFromLocation(this.latitude, this.longitude, 1)?.get(0) ?: return null
        Log.d("getSelectedLocation", "current location address: $address")
        return FixedLocation(
            this.latitude,
            this.longitude,
            address.locality,
            address.adminArea
        )
    }
}