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
import org.koin.android.ext.android.get


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val weatherSettings: WeatherSettings = get()
        val locationRepo: LocationRepo = get()
        val weatherRepo: WeatherRepo = get()
        setContent {
            val coroutineScope = rememberCoroutineScope()
            val machine = remember { Machine(weatherRepo, locationRepo, coroutineScope) }
            LaunchedEffect(Unit) {
                machine.handleAction(Machine.Action.UpdateWeather)
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
