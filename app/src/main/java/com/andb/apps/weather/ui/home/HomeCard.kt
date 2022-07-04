package com.andb.apps.weather.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.ConditionState
import com.andb.apps.weather.Machine
import com.andb.apps.weather.data.model.Conditions
import com.andb.apps.weather.ui.common.ErrorItem
import kotlin.math.roundToInt

@Composable
fun HomeCard(
    conditionState: ConditionState,
    selectedView: HomeView,
    modifier: Modifier = Modifier,
    onFirstItemMeasured: (Dp) -> Unit,
    onAction: (Machine.Action) -> Unit
) {
    val density = LocalDensity.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, MaterialTheme.shapes.large)
            .background(MaterialTheme.colors.background, MaterialTheme.shapes.large)
            .clip(MaterialTheme.shapes.large),
    ) {
        when (conditionState) {
            ConditionState.Error -> ErrorItem(
                title = "Connection Error",
                description = "Click to refresh",
                actionIcon = Icons.Outlined.Refresh,
                leadingIcon = Icons.Outlined.CloudOff,
                modifier = Modifier
                    .onGloballyPositioned { with(density) { onFirstItemMeasured(it.size.height.toDp()) } }
                    .padding(16.dp),
                onClick = { onAction.invoke(Machine.Action.UpdateWeather) }
            )
            ConditionState.Loading -> ErrorItem(
                title = "Loading data...",
                description = "",
                actionIcon = Icons.Outlined.Refresh,
                modifier = Modifier
                    .onGloballyPositioned { with(density) { onFirstItemMeasured(it.size.height.toDp()) } }
                    .padding(16.dp),
                onClick = { onAction.invoke(Machine.Action.UpdateWeather) }
            )
            is ConditionState.Weather -> DailyItems(
                conditions = conditionState.data,
                selectedView = selectedView,
                onFirstCardMeasured = onFirstItemMeasured
            )
        }
        Footer(
            Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            onOpenSettings = {}
        )
    }
}

@Composable
private fun DailyItems(
    conditions: Conditions,
    selectedView: HomeView,
    modifier: Modifier = Modifier,
    onFirstCardMeasured: (Dp) -> Unit
) {
    val globalRanges = GlobalRanges(
        temperatureRange = conditions.days.minOf { it.day.temperatureLow }
            .roundToInt()..conditions.days.maxOf { it.day.temperatureHigh }.roundToInt(),
        windRange = conditions.days.flatMap { day -> day.hourly.map { it.windSpeed } }
            .let { speeds ->
                speeds.minOf { it }.roundToInt()..speeds.maxOf { it }.roundToInt()
            }
    )
    Column(modifier = modifier) {
        conditions.days.forEachIndexed { index, dayItem ->
            val density = LocalDensity.current
            DailyItem(
                dayItem = dayItem,
                globalRanges = globalRanges,
                selectedView = selectedView,
                modifier = Modifier
                    .then(if (index == 0) Modifier.onGloballyPositioned {
                        with(density) {
                            println("measuring first card, height = ${it.size.height.toDp()}")
                            onFirstCardMeasured(it.size.height.toDp())
                        }
                    } else Modifier)
                    .padding(vertical = 24.dp)
            )
        }
    }
}