package com.andb.apps.weather.data.repository

import com.andb.apps.weather.data.local.OFFLINE_JSON
import com.andb.apps.weather.data.local.Prefs
import com.andb.apps.weather.data.model.DarkSkyRequest
import com.andb.apps.weather.data.remote.DarkSkyService
import com.squareup.moshi.Moshi
import retrofit2.Retrofit

class DarkSkyRepoImpl(val moshi: Moshi, val retrofit: Retrofit) : DarkSkyRepo {


    private val service = retrofit.create(DarkSkyService::class.java)

    override suspend fun getForecast(lat: Double, long: Double, apiKey: String): DarkSkyRequest? {
        if (Prefs.DEBUG_OFFLINE) {
            return moshi.adapter(DarkSkyRequest::class.java).fromJson(OFFLINE_JSON)
        }
        return try {
            service.getForecastExtended(apiKey, lat, long)
        } catch (e: Exception) {
            null
        }

    }

}