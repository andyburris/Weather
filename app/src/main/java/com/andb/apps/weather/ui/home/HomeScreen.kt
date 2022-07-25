package com.andb.apps.weather.ui.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.ConditionState
import com.andb.apps.weather.LocationState
import com.andb.apps.weather.Machine
import com.andb.apps.weather.data.model.ConditionCode
import com.andb.apps.weather.ui.location.LocationPicker
import com.andb.apps.weather.ui.test.background.WeatherBackground
import com.andb.apps.weather.util.isDaytime


data class HomeScreenState(
    val selectedLocation: LocationState,
    val currentLocation: LocationState.Current,
    val savedLocations: List<LocationState.Fixed>,
    val conditionState: ConditionState,
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
                    onAction = onAction,
                    modifier = Modifier.padding(it)
                )
                is LocationState.NoLocation -> NoLocation(
                    currentLocation = state.currentLocation,
                    savedLocations = state.savedLocations,
                    modifier = Modifier.padding(it),
                    onAction = onAction,
                )
            }

        }
    )
}

@Composable
private fun NoLocation(
    currentLocation: LocationState.Current,
    savedLocations: List<LocationState.Fixed>,
    modifier: Modifier = Modifier,
    onAction: (Machine.Action) -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        WeatherBackground(conditionCode = ConditionCode.PARTLY_CLOUDY, daytime = null.isDaytime())
        LocationPicker(
            currentLocation = currentLocation,
            savedLocations = savedLocations,
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
    modifier: Modifier = Modifier,
    onAction: (Machine.Action) -> Unit,
) {
    val (selectedView, onSelectView) = remember { mutableStateOf(HomeView.Summary) }
    BoxWithConstraints(modifier = modifier) {
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
                onAction = onAction
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
    }
}

@Composable
private fun LocationContent(
    locationState: LocationState.WithLocation,
    conditionState: ConditionState,
    selectedView: HomeView,
    modifier: Modifier = Modifier,
    onAction: (Machine.Action) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
    ) {
        val firstItemHeight = remember { mutableStateOf(0.dp) }
        val upperContentHeight =
            animateDpAsState(targetValue = this.minHeight - firstItemHeight.value - 12.dp)
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UpperContent(
                locationState = locationState,
                conditionState = conditionState,
                modifier = Modifier.height(upperContentHeight.value)
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
