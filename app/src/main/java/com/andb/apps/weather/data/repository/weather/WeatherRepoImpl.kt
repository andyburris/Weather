package com.andb.apps.weather.data.repository.weather

import com.andb.apps.weather.data.model.Conditions

class WeatherRepoImpl(
    private val providerRepo: ProviderRepo
) : WeatherRepo {
    override suspend fun getForecast(lat: Double, long: Double): Result<Conditions> {
        println("getting forecast")
        val response = providerRepo.getConditions(lat, long)
        println("response = $response")
        return response
    }

}