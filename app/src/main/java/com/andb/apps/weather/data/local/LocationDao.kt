package com.andb.apps.weather.data.local

import androidx.room.*
import com.andb.apps.weather.data.model.Location
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert
    fun saveLocation(location: Location)

    @Update
    fun updateLocation(location: Location)

    @Delete
    fun removeLocation(location: Location)

    @Query("SELECT * FROM Location")
    fun getLocations(): Flow<List<Location>>
}