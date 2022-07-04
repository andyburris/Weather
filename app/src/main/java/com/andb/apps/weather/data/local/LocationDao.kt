package com.andb.apps.weather.data.local

import androidx.room.*
import com.andb.apps.weather.data.model.SavedLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert
    fun saveLocation(location: SavedLocation)

    @Update
    fun updateLocation(location: SavedLocation)

    @Delete
    fun removeLocation(location: SavedLocation)

    @Query("SELECT * FROM SavedLocation")
    fun getLocations(): Flow<List<SavedLocation>>
}