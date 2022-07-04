package com.andb.apps.weather.data.model

import android.content.Context
import androidx.room.Entity
import com.andb.apps.weather.R
import com.google.android.gms.maps.model.LatLng

sealed class SelectedLocation {
    object Current : SelectedLocation()
    data class Fixed(val id: String, val fixedLocation: FixedLocation) : SelectedLocation()
}

@Entity
data class SavedLocation(val id: String, val fixedLocation: FixedLocation)

data class FixedLocation(
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val region: String
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


