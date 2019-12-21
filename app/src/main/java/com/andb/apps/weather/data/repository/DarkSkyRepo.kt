package com.andb.apps.weather.data.repository

import com.andb.apps.weather.data.model.DarkSkyRequest

interface DarkSkyRepo {
    suspend fun getForecast(lat: Double, long: Double, apiKey: String): DarkSkyRequest?
}