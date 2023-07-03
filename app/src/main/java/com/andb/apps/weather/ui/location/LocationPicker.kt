package com.andb.apps.weather.ui.location

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.LocationState
import com.andb.apps.weather.Machine
import com.andb.apps.weather.ui.theme.onBackgroundDivider
import com.andb.apps.weather.ui.theme.onBackgroundSecondary

data class LocationPickerState(
    val currentLocation: LocationState.Current,
    val savedLocations: List<LocationState.Fixed>,
    val searchState: LocationSearchState,
)

data class LocationSearchState(
    val term: String,
    val results: List<LocationState.Fixed>
)

@Composable
fun LocationPicker(
    locationPickerState: LocationPickerState,
    modifier: Modifier = Modifier,
    onAction: (Machine.Action) -> Unit,
) {
    Column(
        modifier = modifier
            .padding(
                bottom = WindowInsets.ime
                    .asPaddingValues()
                    .calculateBottomPadding()
            )
            .then(if (locationPickerState.searchState.term.isEmpty()) Modifier else Modifier.fillMaxHeight())
    ) {
        SearchBar(
            term = locationPickerState.searchState.term,
            placeholder = "Search locations...",
            onTermChange = { onAction.invoke(Machine.Action.SearchLocation(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        )
        Divider(Modifier.fillMaxWidth(), color = MaterialTheme.colors.onBackgroundDivider)
        when {
            locationPickerState.searchState.term.isEmpty() -> SavedLocations(
                currentLocation = locationPickerState.currentLocation,
                savedLocations = locationPickerState.savedLocations,
                modifier = Modifier.padding(vertical = 12.dp),
                onAction = onAction
            )

            else -> SearchLocation(
                locationSearchState = locationPickerState.searchState,
                onAction = onAction
            )
        }
    }
}

@Composable
private fun SavedLocations(
    currentLocation: LocationState.Current,
    savedLocations: List<LocationState.Fixed>,
    modifier: Modifier = Modifier,
    onAction: (Machine.Action) -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = "Saved",
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.onBackgroundSecondary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        )
        LocationItem(location = currentLocation, isSaved = true, onAction = onAction)
        savedLocations.forEach { location ->
            LocationItem(
                location = location,
                isSaved = true,
                onAction = onAction,
            )
        }
    }
}

@Composable
private fun SearchLocation(
    locationSearchState: LocationSearchState,
    modifier: Modifier = Modifier,
    onAction: (Machine.Action) -> Unit,
) {
    LazyColumn(modifier) {
        items(locationSearchState.results) { result ->
            LocationItem(location = result, isSaved = false, onAction = onAction)
        }
    }
}