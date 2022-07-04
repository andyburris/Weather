package com.andb.apps.weather.data.repository.location

import com.andb.apps.weather.data.model.FixedLocation
import com.andb.apps.weather.data.model.SavedLocation
import com.andb.apps.weather.data.model.SelectedLocation
import com.google.android.libraries.places.api.model.AutocompletePrediction
import kotlinx.coroutines.flow.Flow

interface LocationRepo {
    suspend fun getCurrentLocation(): Flow<Result<FixedLocation>>

    fun savedLocations(): Flow<List<SavedLocation>>
    suspend fun getSuggestionsFromSearch(
        term: String,
        currentLat: Double,
        currentLong: Double
    ): List<AutocompletePrediction>

    suspend fun getSuggestionsFromSearch(term: String): List<AutocompletePrediction>
    suspend fun getLocationByID(id: String): SelectedLocation.Fixed?
    suspend fun saveLocation(location: SavedLocation)
    suspend fun deleteLocation(location: SavedLocation)
}