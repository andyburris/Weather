package com.andb.apps.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.*
import com.andb.apps.weather.data.local.LocalSettings
import com.andb.apps.weather.data.local.WeatherSettings
import com.andb.apps.weather.data.repository.location.LocationRepo
import com.andb.apps.weather.data.repository.weather.WeatherRepo
import com.andb.apps.weather.ui.home.HomeScreen
import com.andb.apps.weather.ui.theme.weatherShapes
import com.andb.apps.weather.ui.theme.weatherTypography
import com.google.accompanist.permissions.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.koin.android.ext.android.get


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val weatherSettings: WeatherSettings = get()
        val locationRepo: LocationRepo = get()
        val weatherRepo: WeatherRepo = get()
        setContent {
            val coroutineScope = rememberCoroutineScope()
            val permissionState =
                rememberPermissionState(permission = android.Manifest.permission.ACCESS_FINE_LOCATION)
            val permissionStatus = permissionState.statusFlow()
            val machine = remember {
                Machine(
                    weatherRepo = weatherRepo,
                    locationRepo = locationRepo,
                    hasLocationPermission = permissionStatus.map { it.isGranted },
                    coroutineScope = coroutineScope,
                    onRequestLocationPermission = {
                        println("requesting location permission")
                        permissionState.launchPermissionRequest()
                    },
                )
            }
            val homeScreenState = machine.homeScreen.collectAsState()
            CompositionLocalProvider(LocalSettings provides weatherSettings.uiSettings.collectAsState().value) {
                MaterialTheme(
                    colors = if (LocalSettings.current.darkMode) darkColors() else lightColors(),
                    typography = weatherTypography,
                    shapes = weatherShapes
                ) {
                    HomeScreen(state = homeScreenState.value, onAction = machine::handleAction)
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionState.statusFlow(): Flow<PermissionStatus> {
    val flow = remember { MutableStateFlow(this.status) }
    LaunchedEffect(this.status) {
        flow.value = this@statusFlow.status
    }
    return flow
}