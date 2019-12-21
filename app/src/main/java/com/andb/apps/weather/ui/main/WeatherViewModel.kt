package com.andb.apps.weather.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.andb.apps.weather.data.local.Prefs
import com.andb.apps.weather.data.model.*
import com.andb.apps.weather.data.repository.DarkSkyRepo
import com.andb.apps.weather.data.repository.LocationRepo
import com.andb.apps.weather.util.InitialLiveData
import com.andb.apps.weather.util.mapAsync
import com.andb.apps.weather.util.notNull
import org.threeten.bp.ZoneOffset

class WeatherViewModel(
    private val darkSkyRepo: DarkSkyRepo,
    private val locationRepo: LocationRepo
) : ViewModel() {
    private val location: LiveData<Location?> = locationRepo.getSelectedLocation()
    private val lastRequest: LiveData<DarkSkyRequest?> = location.mapAsync { location ->
        loading.postValue(true)
        Log.d("weatherViewModel", "requesting for $location")
        val rq: DarkSkyRequest? =
            location?.let { darkSkyRepo.getForecast(it.latitude, it.longitude, Prefs.apiKey) }
        Log.d("weatherViewModel", "received $rq")
        loading.postValue(false)
        return@mapAsync rq
    }
    private val currentRequest: LiveData<DarkSkyRequest> = lastRequest.notNull()


    val locationName: LiveData<String> =
        location.map { it?.let { "${it.name}, ${it.region}" } ?: "" }
    val currentTemp: LiveData<Int> =
        Transformations.map(currentRequest) { return@map it.currently.temperature.toInt() }
    val currentFeelsLike: LiveData<Int> =
        Transformations.map(currentRequest) { return@map it.currently.apparentTemperature.toInt() }
    val currentBackground: LiveData<Pair<WeatherIcon, Boolean>> =
        Transformations.map(currentRequest) {
            val isDay =
                it.daily.data[0].sunriseTime < it.currently.time && it.currently.time < it.daily.data[0].sunsetTime
            return@map Pair(it.currently.icon, isDay)
        }
    val minutely: LiveData<Pair<Minutely, ZoneOffset>> =
        Transformations.map(currentRequest) { return@map Pair(it.minutely, it.timezone) }
    val dailyForecasts: LiveData<List<DayItem>> = Transformations.map(currentRequest) { request ->
        return@map request.daily.data.map { conditions ->
            val hourly = request.hourly.data.filter {
                it.time.dayOfMonth == conditions.time.dayOfMonth && (Prefs.dayStart..Prefs.dayEnd).contains(
                    it.time.hour
                )
            }
            DayItem(conditions, hourly, request.timezone)
        }
    }
    val offline: LiveData<Boolean> = lastRequest.map { it == null }
    val loading: InitialLiveData<Boolean> = InitialLiveData(false)


    fun refresh() {
        locationRepo.refresh()
    }

    fun isInitial(): Boolean {
        return location.value == null
    }
}
