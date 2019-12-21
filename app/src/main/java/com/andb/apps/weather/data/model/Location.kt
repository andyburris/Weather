package com.andb.apps.weather.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

@Entity
data class Location(
    @PrimaryKey
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val region: String
) {
    constructor(id: String, latLng: LatLng, name: String, region: String) : this(
        id,
        latLng.latitude,
        latLng.longitude,
        name,
        region
    )
}
