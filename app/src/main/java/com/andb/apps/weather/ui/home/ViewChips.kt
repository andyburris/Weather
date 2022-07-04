package com.andb.apps.weather.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.ui.common.Chip
import com.andb.apps.weather.ui.common.ChipPalette
import com.andb.apps.weather.ui.theme.WeatherColors

@Composable
fun ViewChips(
    selected: HomeView,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onSelect: (HomeView) -> Unit
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(HomeView.values()) { view ->
            val isSelected = view == selected
            Chip(
                label = view.viewName,
                icon = view.icon,
                selected = isSelected,
                selectedPalette = WeatherColors
                    .viewColors(view)
                    .let { ChipPalette(it.background, it.text, Color.Transparent) },
                onClick = { onSelect(view) },
            )
        }
    }
}