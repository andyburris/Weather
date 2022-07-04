package com.andb.apps.weather.ui.test.background.legacy

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.andb.apps.weather.data.model.ConditionCode
import com.andb.apps.weather.ui.test.background.legacy.weatherView.MaterialWeatherView

@Composable
fun LegacyWeatherView(
    conditionCode: ConditionCode,
    daytime: Boolean,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = {
            MaterialWeatherView(it).apply {
                this.setDrawable(true)
            }
        },
        modifier = modifier,
        update = {
            it.setWeather(conditionCode, daytime)
        }
    )
}