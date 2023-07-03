package com.andb.apps.weather.ui.common

sealed class ResultState<T> {
    class Error<T> : ResultState<T>()
    class Loading<T> : ResultState<T>()
    data class Loaded<T>(val value: T) : ResultState<T>()
}