package com.andb.apps.weather.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.andb.apps.weather.data.local.Prefs
import com.andb.apps.weather.data.model.Conditions
import com.andb.apps.weather.data.model.Location
import com.andb.apps.weather.data.model.WeatherIcon
import com.andb.apps.weather.data.repository.LocationRepo
import com.andb.apps.weather.data.repository.WeatherRepo
import com.andb.apps.weather.util.InitialLiveData
import com.andb.apps.weather.util.mapAsync
import com.andb.apps.weather.util.notNull

class WeatherViewModel(
    private val weatherRepo: WeatherRepo,
    private val locationRepo: LocationRepo
) : ViewModel() {
    private val location: LiveData<Location?> = locationRepo.getSelectedLocation()
    private val lastRequest: LiveData<Conditions?> = location.mapAsync { location ->
        loading.postValue(true)
        Log.d("weatherViewModel", "requesting for $location")
        val rq: Conditions? =
            location?.let { weatherRepo.getForecast(it.latitude, it.longitude) }
        Log.d("weatherViewModel", "received $rq")
        loading.postValue(false)
        return@mapAsync rq
    }
    private val currentRequest: LiveData<Conditions> = lastRequest.notNull()


    val locationName = location.map { it?.let { "${it.name}, ${it.region}" } ?: "" }
    val currentTemp = currentRequest.map { it.current.temperature.toInt() }
    val currentFeelsLike = currentRequest.map { it.current.apparentTemperature.toInt() }
    val currentBackground: LiveData<Pair<WeatherIcon, Boolean>> = currentRequest.map { request ->
        val isDay =
            request.current.time in request.days[0].let { it.day.sunriseTime..it.day.sunsetTime }
        return@map Pair(request.current.icon, isDay)
    }
    val minutely = currentRequest.map { it.minutely }
    val dailyForecasts = currentRequest.map { request ->
        request.days.map { dayItem ->
            dayItem.copy(hourly = dayItem.hourly.filter { it.time.hour in Prefs.dayStart..Prefs.dayEnd })
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
