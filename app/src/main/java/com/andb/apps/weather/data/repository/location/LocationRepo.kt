package com.andb.apps.weather.data.repository.location

import com.andb.apps.weather.LocationState
import com.andb.apps.weather.data.model.FixedLocation
import com.google.android.libraries.places.api.model.AutocompletePrediction
import kotlinx.coroutines.flow.Flow

interface LocationRepo {
    suspend fun getCurrentLocation(): Result<FixedLocation>

    fun savedLocations(): Flow<List<LocationState.Fixed>>
    suspend fun getSuggestionsFromSearch(term: String): List<AutocompletePrediction>

    suspend fun getLocationByID(id: String): Result<LocationState.Fixed?>
    suspend fun saveLocation(location: LocationState.Fixed)
    suspend fun deleteLocation(location: LocationState.Fixed)
}