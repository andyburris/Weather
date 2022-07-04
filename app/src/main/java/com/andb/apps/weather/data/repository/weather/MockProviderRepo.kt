package com.andb.apps.weather.data.repository.weather

import com.andb.apps.weather.data.local.OFFLINE_JSON
import com.andb.apps.weather.data.model.Conditions
import com.andb.apps.weather.data.model.darksky.DarkSkyRequest
import com.andb.apps.weather.data.model.darksky.toConditions
import com.squareup.moshi.Moshi

data class MockProviderRepo(
    private val moshi: Moshi,
) : ProviderRepo {
    override suspend fun getConditions(lat: Double, long: Double): Result<Conditions> {
        return Result.success(
            moshi.adapter(DarkSkyRequest::class.java).fromJson(OFFLINE_JSON)!!.toConditions()
        )
    }

}