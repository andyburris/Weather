package com.andb.apps.weather.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.ConditionState
import com.andb.apps.weather.LocationState
import com.andb.apps.weather.Machine
import com.andb.apps.weather.ScreenState
import com.andb.apps.weather.data.model.ConditionCode
import com.andb.apps.weather.data.model.Minutely
import com.andb.apps.weather.ui.common.ProvideIsLoading
import com.andb.apps.weather.ui.location.LocationPicker
import com.andb.apps.weather.ui.location.LocationPickerState
import com.andb.apps.weather.ui.test.background.WeatherBackground
import com.andb.apps.weather.ui.theme.onBackgroundTertiary
import com.andb.apps.weather.util.isDaytime

data class HomeScreenState(
    val selectedLocation: LocationState,
    val locationPickerState: LocationPickerState,
    val conditionState: ConditionState,
) : ScreenState

@Composable
fun HomeScreen(state: HomeScreenState, onAction: (Machine.Action) -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = {
            when (val location = state.selectedLocation) {
                is LocationState.WithLocation -> WithLocation(
                    locationState = location,
                    conditionState = state.conditionState,
                    locationPickerState = state.locationPickerState,
                    modifier = Modifier.padding(it),
                    onAction = onAction
                )

                is LocationState.NoLocation -> NoLocation(
                    locationPickerState = state.locationPickerState,
                    modifier = Modifier.padding(it),
                    onAction = onAction,
                )
            }
        }
    )
}

@Composable
private fun NoLocation(
    locationPickerState: LocationPickerState,
    modifier: Modifier = Modifier,
    onAction: (Machine.Action) -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        WeatherBackground(conditionCode = ConditionCode.PARTLY_CLOUDY, daytime = null.isDaytime())
        LocationPicker(
            locationPickerState = locationPickerState,
            onAction = onAction,
            modifier = Modifier
                .background(MaterialTheme.colors.background)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun WithLocation(
    locationState: LocationState.WithLocation,
    conditionState: ConditionState,
    locationPickerState: LocationPickerState,
    modifier: Modifier = Modifier,
    onAction: (Machine.Action) -> Unit,
) {
    val (selectedView, onSelectView) = remember { mutableStateOf(HomeView.Summary) }
    BoxWithConstraints(modifier = modifier) {
        val (isLocationPickerOpen, setLocationPickerOpen) = remember(locationState) {
            mutableStateOf(false)
        }

        WeatherBackground(
            conditionCode = when (conditionState) {
                is ConditionState.Ok -> conditionState.resource.current.icon
                else -> ConditionCode.PARTLY_CLOUDY
            },
            daytime = conditionState.isDaytime()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {

            LocationContent(
                locationState = locationState,
                conditionState = conditionState,
                selectedView = selectedView,
                modifier = Modifier.weight(1f),
                onAction = onAction,
                onOpenLocationPicker = { setLocationPickerOpen(true) }
            )
            ViewChips(
                selected = selectedView,
                modifier = Modifier
                    .shadow(8.dp)
                    .background(MaterialTheme.colors.background),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                onSelect = onSelectView,
            )
        }

        if (isLocationPickerOpen) {
            BackHandler { setLocationPickerOpen(false) }
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colors.onBackgroundTertiary)
                    .pointerInput(Unit) {}
                    .fillMaxSize(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable { setLocationPickerOpen(false) }
                )
                LocationPicker(
                    locationPickerState = locationPickerState,
                    onAction = onAction,
                    modifier = Modifier
                        .background(MaterialTheme.colors.background, MaterialTheme.shapes.large)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun LocationContent(
    locationState: LocationState.WithLocation,
    conditionState: ConditionState,
    selectedView: HomeView,
    modifier: Modifier = Modifier,
    onAction: (Machine.Action) -> Unit,
    onOpenLocationPicker: () -> Unit,
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = conditionState.isLoading,
        onRefresh = { onAction(Machine.Action.UpdateWeather) })
    BoxWithConstraints(
        modifier = modifier.pullRefresh(pullRefreshState)
    ) {
        PullRefreshIndicator(
            refreshing = conditionState.isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        val firstItemHeight = remember { mutableStateOf(0.dp) }
        val firstItemAnimated = animateDpAsState(firstItemHeight.value)
        val minutelyCardHeight = remember { mutableStateOf(0.dp) }
        val upperContentHeight =
            this.minHeight - firstItemAnimated.value - minutelyCardHeight.value - 16.dp
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(8.dp),
        ) {
            UpperContent(
                locationState = locationState,
                conditionState = conditionState,
                animationInfo = UpperContentAnimationInfo(
                    scrollAmount = with(LocalDensity.current) { scrollState.value.toDp() },
                    upperContentHeight = upperContentHeight,
                ),
                modifier = Modifier.height(upperContentHeight),
                onOpenLocationPicker = onOpenLocationPicker
            )
            val density = LocalDensity.current
            AnimatedVisibility(
                visible = conditionState !is ConditionState.Error,
                modifier = Modifier.onSizeChanged {
                    val newHeight = with(density) { it.height.toDp() }
                    minutelyCardHeight.value = newHeight
                }
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    when (conditionState) {
                        is ConditionState.Ok -> MinutelyCard(minutelyState = conditionState.resource.minutely)
                        else -> ProvideIsLoading {
                            MinutelyCard(
                                minutelyState = Minutely(
                                    "Partly cloudy for the next hour",
                                    listOf()
                                )
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            HomeCard(
                conditionState = conditionState,
                selectedView = selectedView,
                onFirstItemMeasured = { firstItemHeight.value = it },
                onAction = onAction
            )
        }
    }
}
