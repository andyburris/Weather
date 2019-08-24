package com.andb.apps.weather

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class WeatherViewModel : ViewModel() {
    val loading: LiveData<Boolean> = MediatorLiveData()
    private val currentRequest: LiveData<DarkSkyRequest> = MediatorLiveData()

    fun currentTemp() = Transformations.map(currentRequest) {
        return@map it.currently.temperature
    }
}