package com.andb.apps.weather.ui.test

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.data.model.ConditionCode
import com.andb.apps.weather.ui.common.Chip
import com.andb.apps.weather.ui.test.background.RotationState
import com.andb.apps.weather.ui.test.background.WeatherBackground

@Composable
fun TestScreen() {
    val conditionCode = remember { mutableStateOf(ConditionCode.CLEAR) }
    val rotationState = remember { mutableStateOf(RotationState(0f, 0f)) }
    Box() {
        WeatherBackground(
            conditionCode = conditionCode.value,
            modifier = Modifier.fillMaxSize()
        ) { rot2D, rot3D ->
            rotationState.value = RotationState(rot2D, rot3D)
        }
        ConditionChips(
            modifier = Modifier.align(Alignment.BottomCenter),
            selectedConditionCode = conditionCode.value,
            onSelect = { conditionCode.value = it }
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
private fun ConditionChips(
    selectedConditionCode: ConditionCode,
    modifier: Modifier = Modifier,
    onSelect: (ConditionCode) -> Unit
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(ConditionCode.values()) { conditionCode ->
            val isSelected = conditionCode == selectedConditionCode
            Chip(
                label = conditionCode.toString(),
                selected = isSelected,
                modifier = Modifier.clickable { onSelect.invoke(conditionCode) })
        }
    }
}