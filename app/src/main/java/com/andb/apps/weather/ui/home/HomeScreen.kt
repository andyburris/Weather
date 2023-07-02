package com.andb.apps.weather.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.ConditionState
import com.andb.apps.weather.LocationState
import com.andb.apps.weather.Machine
import com.andb.apps.weather.ScreenState
import com.andb.apps.weather.data.model.ConditionCode
import com.andb.apps.weather.ui.location.LocationPicker
import com.andb.apps.weather.ui.test.background.WeatherBackground
import com.andb.apps.weather.ui.theme.onBackgroundTertiary
import com.andb.apps.weather.util.isDaytime

data class HomeScreenState(
    val selectedLocation: LocationState,
    val currentLocation: LocationState.Current,
    val savedLocations: List<LocationState.Fixed>,
    val conditionState: ConditionState,
) : ScreenState

data class LocationPickerState(
    val currentLocation: LocationState.Current,
    val savedLocations: List<LocationState.Fixed>,
)

@Composable
fun HomeScreen(state: HomeScreenState, onAction: (Machine.Action) -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = {
            when (val location = state.selectedLocation) {
                is LocationState.WithLocation -> WithLocation(
                    locationState = location,
                    conditionState = state.conditionState,
                    locationPickerState = LocationPickerState(
                        currentLocation = state.currentLocation,
                        savedLocations = state.savedLocations,
                    ),
                    onAction = onAction,
                    modifier = Modifier.padding(it)
                )

                is LocationState.NoLocation -> NoLocation(
                    locationPickerState = LocationPickerState(
                        currentLocation = state.currentLocation,
                        savedLocations = state.savedLocations,
                    ),
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
            currentLocation = locationPickerState.currentLocation,
            savedLocations = locationPickerState.savedLocations,
            onAction = onAction,
            modifier = Modifier
                .background(MaterialTheme.colors.background, MaterialTheme.shapes.large)
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
                    currentLocation = locationPickerState.currentLocation,
                    savedLocations = locationPickerState.savedLocations,
                    onAction = onAction,
                    modifier = Modifier
                        .background(MaterialTheme.colors.background, MaterialTheme.shapes.large)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun LocationContent(
    locationState: LocationState.WithLocation,
    conditionState: ConditionState,
    selectedView: HomeView,
    modifier: Modifier = Modifier,
    onAction: (Machine.Action) -> Unit,
    onOpenLocationPicker: () -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
    ) {
        val firstItemHeight = remember { mutableStateOf(0.dp) }
        val upperContentHeight =
            animateDpAsState(targetValue = this.minHeight - firstItemHeight.value - 12.dp)
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UpperContent(
                locationState = locationState,
                conditionState = conditionState,
                scrollAmount = with(LocalDensity.current) {
                    scrollState.value.toDp().coerceAtMost(upperContentHeight.value - 128.dp)
                },
                modifier = Modifier.height(upperContentHeight.value),
                onOpenLocationPicker = onOpenLocationPicker
            )
            HomeCard(
                conditionState = conditionState,
                selectedView = selectedView,
                onFirstItemMeasured = { firstItemHeight.value = it },
                onAction = onAction
            )
        }
    }
}
