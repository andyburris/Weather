package com.andb.apps.weather.ui.test

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.data.model.ConditionCode
import com.andb.apps.weather.ui.common.Chip
import com.andb.apps.weather.ui.test.background.RotationState
import com.andb.apps.weather.ui.test.background.WeatherBackground
import com.andb.apps.weather.ui.test.background.legacy.LegacyWeatherView

@Composable
fun TestScreen() {
    val daytime = remember { mutableStateOf(true) }
    val conditionCode = remember { mutableStateOf(ConditionCode.CLEAR) }
    val legacyWeatherView = remember { mutableStateOf(false) }
    val rotationState = remember { mutableStateOf(RotationState(0f, 0f)) }
    Box {
        if (!legacyWeatherView.value) {
            WeatherBackground(
                conditionCode = conditionCode.value,
                daytime = daytime.value,
                modifier = Modifier.fillMaxSize()
            ) { rot2D, rot3D ->
                rotationState.value = RotationState(rot2D, rot3D)
            }
        } else {
            LegacyWeatherView(
                conditionCode = conditionCode.value,
                daytime = daytime.value,
                modifier = Modifier.fillMaxSize()
            )
        }
        ConditionChips(
            modifier = Modifier.align(Alignment.BottomCenter),
            selectedConditionCode = conditionCode.value,
            onSelectConditionCode = { conditionCode.value = it },
            daytime = daytime.value,
            onSelectDaytime = { daytime.value = it },
            legacyView = legacyWeatherView.value,
            onSelectView = { legacyWeatherView.value = it },
        )
        Text(
            text = "2D: ${Math.toDegrees(rotationState.value.rotation2D.toDouble())}°\n3D: ${
                Math.toDegrees(
                    rotationState.value.rotation3D.toDouble()
                )
            }°",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp),
            color = Color.White
        )
    }
}

@Composable
fun ConditionChips(
    daytime: Boolean,
    selectedConditionCode: ConditionCode,
    legacyView: Boolean,
    modifier: Modifier = Modifier,
    onSelectDaytime: (Boolean) -> Unit,
    onSelectConditionCode: (ConditionCode) -> Unit,
    onSelectView: (Boolean) -> Unit,
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Chip(
                label = "Day",
                selected = daytime,
                modifier = Modifier.clickable { onSelectDaytime.invoke(true) })
        }
        item {
            Chip(
                label = "Night",
                selected = !daytime,
                modifier = Modifier.clickable { onSelectDaytime.invoke(false) })
        }
        item { Divider(Modifier.height(32.dp)) }
        items(ConditionCode.values()) { conditionCode ->
            val isSelected = conditionCode == selectedConditionCode
            Chip(
                label = conditionCode.toString().lowercase().capitalize(Locale.current)
                    .filter { it != '_' },
                selected = isSelected,
                modifier = Modifier.clickable { onSelectConditionCode.invoke(conditionCode) }
            )
        }
        item { Divider(Modifier.height(32.dp)) }
        item {
            Chip(
                label = "New",
                selected = !legacyView,
                modifier = Modifier.clickable { onSelectView.invoke(false) })
        }
        item {
            Chip(
                label = "Legacy",
                selected = legacyView,
                modifier = Modifier.clickable { onSelectView.invoke(true) })
        }
    }
}