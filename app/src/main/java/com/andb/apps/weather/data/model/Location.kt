package com.andb.apps.weather.data.model

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.andb.apps.weather.R
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

    fun getText(context: Context): String {
        return context.resources.getString(R.string.location_placeholder).format(name, region)
    }
}
