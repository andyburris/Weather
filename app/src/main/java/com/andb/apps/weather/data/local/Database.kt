package com.andb.apps.weather.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.andb.apps.weather.data.model.SavedLocation

@Database(entities = [SavedLocation::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun locationDao(): LocationDao
}