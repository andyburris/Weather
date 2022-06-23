package com.andb.apps.weather.data.repository

import com.andb.apps.weather.BuildConfig
import com.andb.apps.weather.data.local.OFFLINE_JSON
import com.andb.apps.weather.data.local.PROVIDER_DARK_SKY
import com.andb.apps.weather.data.local.Prefs
import com.andb.apps.weather.data.model.Conditions
import com.andb.apps.weather.data.model.climacell.ClimacellRequest
import com.andb.apps.weather.data.model.climacell.toConditions
import com.andb.apps.weather.data.model.darksky.DarkSkyRequest
import com.andb.apps.weather.data.model.darksky.toConditions
import com.andb.apps.weather.data.remote.ClimacellService
import com.andb.apps.weather.data.remote.DarkSkyService
import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class WeatherRepoImpl(
    private val moshi: Moshi,
    darkSkyRetrofit: Retrofit,
    climacellRetrofit: Retrofit
) : WeatherRepo {


    private val darkSkyService = darkSkyRetrofit.create(DarkSkyService::class.java)
    private val climacellService = climacellRetrofit.create(ClimacellService::class.java)

    override suspend fun getForecast(lat: Double, long: Double): Conditions? {
        return when (Prefs.MULTIPLE_PROVIDERS) {
            true -> getMultipleForecast(lat, long)
            false -> getDarkSkyRequest(lat, long, Prefs.apiKey)?.toConditions()
        }

    }

    private suspend fun getMultipleForecast(lat: Double, long: Double): Conditions? {
        return when (Prefs.providers.first().id) {
            PROVIDER_DARK_SKY -> getDarkSkyRequest(lat, long, Prefs.apiKey)?.toConditions()
            //PROVIDER_CLIMACELL -> getClimacellRequest(lat, long, apiKey)?.toConditions()
            //else -> throw Error("No provider specified")
            else -> getClimacellRequest(lat, long, BuildConfig.CLIMACELL_API_KEY)?.toConditions()
        }
    }

    private suspend fun getDarkSkyRequest(
        lat: Double,
        long: Double,
        apiKey: String
    ): DarkSkyRequest? {
        if (Prefs.DEBUG_OFFLINE) {
            return moshi.adapter(DarkSkyRequest::class.java).fromJson(OFFLINE_JSON)
        }

        return try {
            darkSkyService.getForecastExtended(apiKey, lat, long)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun getClimacellRequest(
        lat: Double,
        long: Double,
        apiKey: String
    ): ClimacellRequest? {
        try {
/*            val current = asyncIO { climacellService.getCurrent(apiKey, lat, long, UnitType.US) }
            val nowcast = asyncIO { climacellService.getNowcast(apiKey, lat, long, UnitType.US, ZonedDateTime.now().plusHours(1).plusMinutes(1).format(DateTimeFormatter.ISO_INSTANT)) }
            val hourly = asyncIO { climacellService.getHourly(apiKey, lat, long, UnitType.US) }
            val daily = asyncIO { climacellService.getDaily(apiKey, lat, long, UnitType.US) }
            return ClimacellRequest(current.await(), nowcast.await(), hourly.await(), daily.await())*/
            val current = climacellService.getCurrent(apiKey, lat, long, Prefs.units)
            val nowcast = climacellService.getNowcast(
                apiKey,
                lat,
                long,
                Prefs.units,
                ZonedDateTime.now().plusHours(1).plusMinutes(1)
                    .format(DateTimeFormatter.ISO_INSTANT)
            )
            val hourly = climacellService.getHourly(apiKey, lat, long, Prefs.units)
            val daily = climacellService.getDaily(apiKey, lat, long, Prefs.units)
            return ClimacellRequest(current, nowcast, hourly, daily)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}