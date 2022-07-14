package com.andb.apps.weather.util

import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun PlacesClient.autocompleteWith(
    query: String,
    block: PlacesAutocompleteDSL.() -> Unit
): Result<FindAutocompletePredictionsResponse?> {
    val autocompleteDSL = PlacesAutocompleteDSL(query)
    return this.findAutocompletePredictions(autocompleteDSL.build()).getSync()
}

suspend fun PlacesClient.getPlaceByID(
    id: String,
    fields: List<Place.Field>
): Result<FetchPlaceResponse?> {
    return fetchPlace(FetchPlaceRequest.newInstance(id, fields)).getSync()
}

class PlacesAutocompleteDSL(val query: String) {
    var latLong: Pair<Double, Double>? = null
    var radius = 1.0

    fun build(): FindAutocompletePredictionsRequest {
        var builder = FindAutocompletePredictionsRequest.builder().setQuery(query)
        val builtLatLong = latLong
        if (builtLatLong != null && radius > 0) {
            val bias = RectangularBounds.newInstance(
                builtLatLong.toLatLng(-radius),
                builtLatLong.toLatLng(radius)
            )
            builder = builder.setLocationBias(bias)
        }
        return builder.build()
    }

    private fun Pair<Double, Double>.toLatLng() = LatLng(first, second)
    private fun Pair<Double, Double>.toLatLng(offset: Double) =
        LatLng(first + offset, second + offset)
}

suspend fun <T> Task<T>.getSync(): Result<T?> {
    return suspendCoroutine { cont ->
        this.addOnCompleteListener {
            cont.resume(runCatching { it.result })
        }
    }
}

@Throws(SecurityException::class)
fun FusedLocationProviderClient.locationFlow(): Flow<Location> = callbackFlow {
    this@locationFlow.lastLocation.addOnCompleteListener { this.trySend(it.result) }
}