package com.andb.apps.weather.ui.location

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.LocationState
import com.andb.apps.weather.Machine
import com.andb.apps.weather.ui.common.ErrorItem
import com.andb.apps.weather.ui.theme.onBackgroundOverlay
import com.andb.apps.weather.ui.theme.onBackgroundSecondary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationItem(
    location: LocationState,
    modifier: Modifier = Modifier,
    onSelectLocation: (Machine.Action.SelectLocation) -> Unit
) {
    Row(
        modifier = modifier
            .clickable { onSelectLocation.invoke(location.toSelectLocationAction()) }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = when (location) {
            is LocationState.WithLocation -> Alignment.CenterVertically
            is LocationState.NoLocation -> Alignment.Top
        }
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colors.onBackgroundOverlay, CircleShape)
                .padding(8.dp),
        ) {
            Icon(
                imageVector = when (location) {
                    is LocationState.Current -> Icons.Outlined.MyLocation
                    is LocationState.Fixed -> Icons.Outlined.NearMe
                },
                contentDescription = null,
                tint = MaterialTheme.colors.onBackgroundSecondary,
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = when (location) {
                    is LocationState.Current -> "Current Location"
                    is LocationState.Fixed -> location.location.let { "${it.name}, ${it.region}" }
                },
                color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.subtitle1,
            )

            when (location) {
                is LocationState.Current.NoPermission -> {
                    val locationPermissionState =
                        rememberPermissionState(permission = android.Manifest.permission.ACCESS_FINE_LOCATION)
                    LaunchedEffect(locationPermissionState.status) {
                        if (locationPermissionState.status is PermissionStatus.Granted) onSelectLocation.invoke(
                            Machine.Action.SelectLocation.Current
                        )
                    }
                    ErrorItem(
                        title = "Permission Needed",
                        actionIcon = Icons.Outlined.ChevronRight,
                        onClick = { locationPermissionState.launchPermissionRequest() }
                    )
                }
                is LocationState.Current.NoAccess -> ErrorItem(
                    title = "Permission Needed",
                    actionIcon = Icons.Outlined.ChevronRight,
                    onClick = { onSelectLocation.invoke(Machine.Action.SelectLocation.Current) }
                )
            }
        }
    }
}

private fun LocationState.toSelectLocationAction(): Machine.Action.SelectLocation = when (this) {
    is LocationState.Current -> Machine.Action.SelectLocation.Current
    is LocationState.Fixed -> Machine.Action.SelectLocation.Fixed(this.id)
}