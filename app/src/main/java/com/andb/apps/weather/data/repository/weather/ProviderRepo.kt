package com.andb.apps.weather.data.repository.weather

import com.andb.apps.weather.data.model.Conditions

interface ProviderRepo {
    suspend fun getConditions(
        lat: Double,
        long: Double,
    ): Result<Conditions>
}