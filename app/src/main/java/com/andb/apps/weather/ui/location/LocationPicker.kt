package com.andb.apps.weather.ui.location

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.LocationState
import com.andb.apps.weather.Machine
import com.andb.apps.weather.ui.theme.onBackgroundDivider
import com.andb.apps.weather.ui.theme.onBackgroundSecondary

@Composable
fun LocationPicker(
    currentLocation: LocationState.Current,
    savedLocations: List<LocationState.Fixed>,
    modifier: Modifier = Modifier,
    onSelectLocation: (Machine.Action.SelectLocation) -> Unit,
) {
    val searchTerm = remember { mutableStateOf("") }
    Column(modifier = modifier) {
        SearchBar(
            term = searchTerm.value,
            placeholder = "Search locations...",
            onTermChange = { searchTerm.value = it },
            modifier = Modifier.padding(24.dp)
        )
        Divider(Modifier.fillMaxWidth(), color = MaterialTheme.colors.onBackgroundDivider)
        when {
            searchTerm.value.isEmpty() -> SavedLocations(
                currentLocation = currentLocation,
                savedLocations = savedLocations,
                modifier = Modifier.padding(vertical = 12.dp),
                onSelectLocation = onSelectLocation
            )
        }
    }
}

@Composable
private fun SavedLocations(
    currentLocation: LocationState.Current,
    savedLocations: List<LocationState.Fixed>,
    modifier: Modifier = Modifier,
    onSelectLocation: (Machine.Action.SelectLocation) -> Unit,
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
        LocationItem(location = currentLocation, onSelectLocation = onSelectLocation)
        savedLocations.forEach { location ->
            LocationItem(location = location, onSelectLocation = onSelectLocation)
        }
    }
}