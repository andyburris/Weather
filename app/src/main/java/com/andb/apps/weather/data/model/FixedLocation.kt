package com.andb.apps.weather.data.model

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.andb.apps.weather.LocationState
import com.andb.apps.weather.R
import com.google.android.gms.maps.model.LatLng

sealed class SelectedLocation {
    object Current : SelectedLocation()
    data class Fixed(val id: String) : SelectedLocation()
}

@Entity
data class SavedLocation(
    @PrimaryKey
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val region: String,
)

fun SavedLocation.toFixedLocationState() = LocationState.Fixed(
    id = id,
    location = FixedLocation(
        latitude = latitude,
        longitude = longitude,
        name = name,
        region = region,
    )
)

fun LocationState.Fixed.toSavedLocation() = SavedLocation(
    id = id,
    latitude = location.latitude,
    longitude = location.longitude,
    name = location.name,
    region = location.region,
)

data class FixedLocation(
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val region: String,
) {
    constructor(latLng: LatLng, name: String, region: String) : this(
        latLng.latitude,
        latLng.longitude,
        name,
        region
    )

    fun getText(context: Context): String {
        return context.resources.getString(R.string.location_placeholder).format(name, region)
    }
}


