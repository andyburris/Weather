package com.andb.apps.weather.data.repository

import com.andb.apps.weather.data.model.Conditions

interface WeatherRepo {
    suspend fun getForecast(lat: Double, long: Double): Conditions?
}