package com.andb.apps.weather.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Palette
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.Machine
import com.andb.apps.weather.Screen
import com.andb.apps.weather.ui.theme.onBackgroundSecondary

@Composable
fun SettingsScreen(
    onAction: (Machine.Action) -> Unit,
) {
    Scaffold(
        topBar = {
             TopAppBar(
                 navigationIcon = {
                     IconButton(onClick = { onAction(Machine.Action.OpenScreen(Screen.Home)) }) {
                         Icon(Icons.Default.Clear, contentDescription = "Back home")
                     }
                 },
                 title = {},
             )
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(top = 64.dp)
        ) {
            Text(text = "Settings", style = MaterialTheme.typography.h1)
            SettingsItem(
                title = "Theme",
                description = "Light",
                icon = Icons.Default.Palette,
            )
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    Row(
        modifier = modifier
            .height(64.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null)
        Column {
            Text(text = title, style = MaterialTheme.typography.subtitle1)
            if (description != null) {
                Text(text = description, style = MaterialTheme.typography.body1, color = MaterialTheme.colors.onBackgroundSecondary)
            }
        }
    }
    
}

/*
data class SettingsState(
    val preferences: Preferences,
    val screen: Screen
) {

}

class PreferenceState<T>(
    private val preferences: State<Preferences>,
    private val preferenceKey: Preferences.Key<T>,
    private val onEdit: (T) -> Unit,
) : MutableState<T> {
    override var value: T = preferences.value[preferenceKey] ?: throw Error("preferenceKey $preferenceKey does not exist")

}

@Composable
fun <T> Flow<T>.collectAsMutableState(initial: T, setValue: (T) -> Unit): MutableState<T> {
    return this.collectAsState(initial = initial)

}*/
