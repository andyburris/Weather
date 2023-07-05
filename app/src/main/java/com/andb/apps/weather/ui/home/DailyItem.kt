package com.andb.apps.weather.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.data.model.DayItem
import com.andb.apps.weather.data.model.HourlyConditions
import com.andb.apps.weather.ui.common.FastIntRange
import com.andb.apps.weather.ui.common.asIntRange
import com.andb.apps.weather.ui.theme.WeatherColors
import com.andb.apps.weather.ui.theme.onBackgroundDivider
import com.andb.apps.weather.ui.theme.onBackgroundSecondary
import com.andb.apps.weather.ui.theme.onBackgroundTertiary
import com.andb.apps.weather.util.size
import kotlinx.coroutines.flow.SharedFlow
import java.time.LocalDateTime
import kotlin.math.roundToInt

data class GlobalRanges(
    val temperatureRange: FastIntRange,
    val windRange: FastIntRange,
)


@Composable
fun DailyItem(
    dayItem: DayItem,
    globalRanges: GlobalRanges,
    selectedView: HomeView,
    scrollDispatcher: SharedFlow<Float>,
    modifier: Modifier = Modifier,
    onDispatchScroll: (dragAmount: Float) -> Unit
) {
    Column(
        modifier = modifier,
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
                globalScale = globalRanges.temperatureRange.asIntRange(),
                modifier = Modifier.weight(1f)
            )
        }
        AnimatedVisibility(visible = selectedView != HomeView.Summary) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                HomeViewWidget(
                    selectedView = selectedView,
                    dayItem = dayItem,
                    globalRanges = globalRanges,
                    scrollDispatcher = scrollDispatcher,
                    onDispatchScroll = onDispatchScroll,
                )
                /*
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(horizontal = 24.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val barColor = WeatherColors.viewColors(selectedView).graphic
                                    dayItem.hourly.forEach { hourlyConditions ->
                                        TestItem(
                                            selectedView = selectedView,
                                            hourlyConditions = hourlyConditions,
                                            globalRanges = globalRanges,
                                            barColor = barColor,
                                            barHeight = 48.dp,
                                            Modifier
                                                .height(60.dp)
                                                .recomposeHighlighter(),
                                        )
                                        HourlyItem(
                                            selectedView = selectedView,
                                            hourlyConditions = hourlyConditions,
                                            globalRanges = globalRanges,
                                            barColor = barColor,
                                            barHeight = 48.dp,
                                            modifier = Modifier
                                                .height(60.dp)
                                        )
                                    }
                                }
                */
            }
        }
    }
}

private val GraphBarShape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)

@Composable
private fun TestItem(
    selectedView: HomeView,
    hourlyConditions: HourlyConditions,
    globalRanges: GlobalRanges,
    barColor: Color,
    barHeight: Dp,
    modifier: Modifier = Modifier,
) {
    val x = remember { hourlyConditions }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Bottom),
    ) {
        Text(
            text = "50%",
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onBackground,
        )
        Box(
            modifier = Modifier
                .background(
                    color = Color.Red,
                    shape = GraphBarShape
                )
                .height(60.dp * .5f)
                .width(32.dp)
        )
        Text(
            text = "1pm",
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onBackgroundSecondary,
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
            val scalePercent = dailyScale.size.toFloat() / globalScale.size.toFloat()
            val width = this.maxWidth * scalePercent
            val paddingStart =
                this.maxWidth * ((dailyScale.first - globalScale.first) / globalScale.size.toFloat())
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

data class SyncScrollState(
    val currentlyDragging: DayItem,
    val hourOffset: Int,
    val lazyListState: LazyListState
)
@Composable
private fun HomeViewWidget(
    selectedView: HomeView,
    dayItem: DayItem,
    globalRanges: GlobalRanges,
    scrollDispatcher: SharedFlow<Float>,
    modifier: Modifier = Modifier,
    onDispatchScroll: (dragAmount: Float) -> Unit,
) {
    val barColor = WeatherColors.viewColors(selectedView).graphic
    when (selectedView) {
        HomeView.Summary -> Summary(
            dayItem = dayItem,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        else -> {
            val scrollState = rememberScrollState()
            LaunchedEffect(dayItem.day.date.dayOfMonth) {
//                println("collecting scrollDispatcher, today = ${dayItem.day.date.dayOfMonth}")
                scrollDispatcher.collect { dragAmount ->
//                    println("collecting scroll of ${dragAmount}px, today = ${dayItem.day.date.dayOfMonth}")
                    scrollState.scroll { this.scrollBy(dragAmount) }
                }
            }
//            val thisHourOffset = dayItem.hourly.minBy { it.time }.time.hour
            Row(
                modifier = modifier
                    .pointerInput(Unit) {
                        this.detectDragGesturesAfterLongPress(
                            onDrag = { change, dragAmount -> onDispatchScroll.invoke(-dragAmount.x) }
                        )
                    }
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                dayItem.hourly.map { hourlyConditions ->
                    val inPast =
                        (hourlyConditions.time.dayOfMonth <= LocalDateTime.now().dayOfMonth) && (hourlyConditions.time.hour < LocalDateTime.now().hour)
                    HourlyItem(
                        selectedView = selectedView,
                        hourlyConditions = hourlyConditions,
                        globalRanges = globalRanges,
                        barColor = barColor,
                        modifier = Modifier
                            .height(60.dp)
                            .graphicsLayer(alpha = if (inPast) 0.5f else 1.0f)
                    )
                }
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