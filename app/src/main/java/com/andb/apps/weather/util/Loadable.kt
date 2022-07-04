package com.andb.apps.weather.util

sealed class Loadable<T, E> {
    abstract val isLoading: Boolean

    data class NotLoaded(override val isLoading: Boolean) : Loadable<Nothing, Nothing>()
    data class Error<E>(val error: E, override val isLoading: Boolean) : Loadable<Nothing, E>()
    data class Ok<T>(val resource: T, override val isLoading: Boolean) : Loadable<T, Nothing>()
}