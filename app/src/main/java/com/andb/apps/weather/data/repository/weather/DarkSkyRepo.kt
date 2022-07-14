package com.andb.apps.weather.data.repository.weather

import com.andb.apps.weather.data.model.Conditions
import com.andb.apps.weather.data.model.darksky.toConditions
import com.andb.apps.weather.data.remote.DarkSkyService

class DarkSkyRepo(
    val darkSkyService: DarkSkyService,
    val apiKey: String,
) : ProviderRepo {
    override suspend fun getConditions(lat: Double, long: Double): Result<Conditions> =
        runCatching {
            darkSkyService.getForecastExtended(apiKey, lat, long).toConditions()
        }
}