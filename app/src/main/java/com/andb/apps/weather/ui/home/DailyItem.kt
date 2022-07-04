package com.andb.apps.weather.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.data.model.DayItem
import com.andb.apps.weather.ui.theme.onBackgroundDivider
import com.andb.apps.weather.ui.theme.onBackgroundSecondary
import com.andb.apps.weather.ui.theme.onBackgroundTertiary
import com.andb.apps.weather.util.size
import kotlin.math.roundToInt

data class GlobalRanges(
    val temperatureRange: IntRange,
    val windRange: IntRange,
)

@Composable
fun DailyItem(
    dayItem: DayItem,
    globalRanges: GlobalRanges,
    selectedView: HomeView,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = dayItem.day.date.dayOfWeek.name.lowercase().capitalize(Locale.current),
                color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.weight(1f),
            )
            TemperatureWidget(
                dailyScale = dayItem.day.temperatureLow.roundToInt()..dayItem.day.temperatureHigh.roundToInt(),
                globalScale = globalRanges.temperatureRange,
                modifier = Modifier.weight(1f)
            )
        }
        HomeViewWidget(
            selectedView = selectedView,
            dayItem = dayItem,
            globalRanges = globalRanges
        )
    }
}

@Composable
private fun TemperatureWidget(
    dailyScale: IntRange,
    globalScale: IntRange,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${dailyScale.first}°",
            color = MaterialTheme.colors.onBackgroundTertiary,
            style = MaterialTheme.typography.body2
        )
        BoxWithConstraints(
            modifier = Modifier
                .background(MaterialTheme.colors.onBackgroundDivider, CircleShape)
                .weight(1f)
        ) {
            val width = this.maxWidth * (dailyScale.size.toFloat() / globalScale.size.toFloat())
            val paddingStart =
                width * ((dailyScale.first - globalScale.first) / globalScale.size.toFloat())
            Box(
                modifier = Modifier
                    .padding(start = paddingStart)
                    .background(MaterialTheme.colors.onBackgroundSecondary, CircleShape)
                    .height(4.dp)
                    .width(width)
            )
        }
        Text(
            text = "${dailyScale.last}°",
            color = MaterialTheme.colors.onBackgroundSecondary,
            style = MaterialTheme.typography.body2
        )
    }
}

@Composable
private fun HomeViewWidget(
    selectedView: HomeView,
    dayItem: DayItem,
    globalRanges: GlobalRanges,
    modifier: Modifier = Modifier,
) {
    when (selectedView) {
        HomeView.Summary -> Summary(
            dayItem = dayItem,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        else -> LazyRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom,
            contentPadding = PaddingValues(horizontal = 24.dp),
        ) {
            items(dayItem.hourly) { hourlyConditions ->
                HourlyItem(
                    selectedView = selectedView,
                    hourlyConditions = hourlyConditions,
                    globalRanges = globalRanges,
                    modifier = Modifier.height(60.dp)
                )
            }
        }
    }
}

@Composable
private fun Summary(
    dayItem: DayItem,
    modifier: Modifier = Modifier,
) {
    Text(
        text = dayItem.day.summary,
        style = MaterialTheme.typography.body1,
        color = MaterialTheme.colors.onBackgroundSecondary,
        modifier = modifier,
    )
}