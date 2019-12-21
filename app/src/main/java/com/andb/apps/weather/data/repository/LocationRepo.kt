package com.andb.apps.weather.data.repository

import androidx.lifecycle.LiveData
import com.andb.apps.weather.data.model.Location
import com.google.android.libraries.places.api.model.AutocompletePrediction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

@ExperimentalCoroutinesApi
interface LocationRepo {
    fun getAllLocations(): Flow<List<Location>>
    fun getSelectedLocation(): LiveData<Location?>
    suspend fun getSuggestionsFromSearch(
        term: String,
        currentLat: Double,
        currentLong: Double
    ): List<AutocompletePrediction>

    suspend fun getSuggestionsFromSearch(term: String): List<AutocompletePrediction>
    suspend fun getLocationByID(id: String): Location?
    suspend fun saveLocation(location: Location)
    suspend fun deleteLocation(location: Location)
    fun selectLocation(id: String)
    fun refresh()
}