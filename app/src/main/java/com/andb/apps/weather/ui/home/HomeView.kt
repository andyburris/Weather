package com.andb.apps.weather.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class HomeView(val viewName: String, val icon: ImageVector) {
    Summary("Summary", Icons.Outlined.List),
    Temperature("Temperature", Icons.Outlined.Thermostat),
    Rain("Rain", Icons.Outlined.WaterDrop),
    UV("UV Index", Icons.Outlined.NewReleases),
    Wind("Wind", Icons.Outlined.Air),
}