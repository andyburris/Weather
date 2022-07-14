package com.andb.apps.weather.data.local

import androidx.compose.runtime.staticCompositionLocalOf
import com.russhwolf.settings.coroutines.FlowSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

val DEBUG_OFFLINE = false

data class WeatherSettings(
    private val settings: FlowSettings,
) {
    val apiKey: Flow<String> = settings.getStringFlow("apiKey")
    val offlineData: StateFlow<Boolean> = MutableStateFlow(DEBUG_OFFLINE)
    val uiSettings: StateFlow<UISettings> = MutableStateFlow(DefaultUISettings)
}

interface UISettings {
    val darkMode: Boolean
    val dayHourRange: IntRange
    val time24HrFormat: Boolean
}

object DefaultUISettings : UISettings {
    override val darkMode: Boolean = false
    override val dayHourRange: IntRange = 7..23
    override val time24HrFormat: Boolean = false
}

val LocalSettings = staticCompositionLocalOf<UISettings> { DefaultUISettings }