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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.ConditionState
import com.andb.apps.weather.Machine
import com.andb.apps.weather.Screen
import com.andb.apps.weather.data.model.Conditions
import com.andb.apps.weather.ui.common.ErrorItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
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
    val measureItemModifier =
        Modifier.onGloballyPositioned { with(density) { onFirstItemMeasured(it.size.height.toDp()) } }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, MaterialTheme.shapes.large)
            .background(MaterialTheme.colors.background, MaterialTheme.shapes.large)
            .clip(MaterialTheme.shapes.large),
    ) {
        when (conditionState) {
            is ConditionState.Error -> when (conditionState.isLoading) {
                true -> ConditionLoadingItem(measureItemModifier)
                false -> ConditionErrorItem(measureItemModifier) { onAction.invoke(Machine.Action.UpdateWeather) }
            }
            is ConditionState.NotLoaded -> ConditionLoadingItem(measureItemModifier)
            is ConditionState.Ok -> DailyItems(
                conditions = conditionState.resource,
                selectedView = selectedView,
                onFirstCardMeasured = onFirstItemMeasured
            )
        }
        Footer(
            Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            onOpenSettings = { onAction.invoke(Machine.Action.OpenScreen(Screen.Settings)) }
        )
    }
}

@Composable
private fun ConditionErrorItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    ErrorItem(
        title = "Connection Error",
        description = "Click to refresh",
        actionIcon = Icons.Outlined.Refresh,
        leadingIcon = Icons.Outlined.CloudOff,
        modifier = Modifier.padding(16.dp),
        onClick = onClick
    )
}

@Composable
private fun ConditionLoadingItem(
    modifier: Modifier = Modifier,
) {
    ErrorItem(
        title = "Loading data...",
        description = "",
        actionIcon = Icons.Outlined.Refresh,
        modifier = modifier.padding(16.dp),
    )
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
            val coroutineScope = rememberCoroutineScope()
            val scrollDispatcher: MutableSharedFlow<Float> = remember { MutableSharedFlow() }
            DailyItem(
                dayItem = dayItem,
                globalRanges = globalRanges,
                selectedView = selectedView,

                modifier = Modifier
                    .then(if (index == 0) Modifier.onGloballyPositioned {
                        with(density) {
                            onFirstCardMeasured(it.size.height.toDp())
                        }
                    } else Modifier)
                    .padding(vertical = 24.dp),
                scrollDispatcher = scrollDispatcher,
                onDispatchScroll = { dragAmount ->
                    coroutineScope.launch {
                        scrollDispatcher.emit(dragAmount)
                        println("dispatching scroll of ${dragAmount}px")
                    }
                }
            )
        }
    }
}