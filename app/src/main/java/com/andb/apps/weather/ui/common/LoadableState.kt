package com.andb.apps.weather.ui.common

sealed class LoadableState<T> {
    class Loading<T> : LoadableState<T>()
    data class Ok<T>(val value: T) : LoadableState<T>()
}

fun <T, R> LoadableState<T>.map(transform: (T) -> R): LoadableState<R> = when (this) {
    is LoadableState.Loading -> LoadableState.Loading<R>()
    is LoadableState.Ok -> LoadableState.Ok(transform(this.value))
}
