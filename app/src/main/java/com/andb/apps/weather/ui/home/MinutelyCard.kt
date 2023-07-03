package com.andb.apps.weather.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.data.model.Minutely
import com.andb.apps.weather.data.model.MinutelyConditions
import com.andb.apps.weather.ui.common.placeholderOnLoading
import com.andb.apps.weather.ui.theme.onBackgroundSecondary
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.entry.entryModelOf

@Composable
fun MinutelyCard(
    minutelyState: Minutely,
    modifier: Modifier = Modifier,
) {
    val isExpanded = remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.background, MaterialTheme.shapes.large)
            .clip(MaterialTheme.shapes.large),
    ) {
        Row(
            modifier = modifier
                .clickable { isExpanded.value = !isExpanded.value }
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = minutelyState.summary,
                modifier = Modifier
                    .weight(1f)
                    .placeholderOnLoading()
            )
            val iconRotation = animateFloatAsState(targetValue = if (isExpanded.value) 180f else 0f)
            Icon(
                Icons.Outlined.KeyboardArrowUp,
                contentDescription = "Expand next hour",
                tint = MaterialTheme.colors.onBackgroundSecondary,
                modifier = Modifier
                    .placeholderOnLoading()
                    .rotate(iconRotation.value),
            )
        }
        AnimatedVisibility(visible = isExpanded.value) {
            MinutelyGraph(
                data = minutelyState.data,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MinutelyGraph(
    data: List<MinutelyConditions>,
    modifier: Modifier = Modifier,
) {
    val entryModel = entryModelOf(*data.map { it.precipChance }.toTypedArray())
    val chartStyle = currentChartStyle.copy(

    )
    ProvideChartStyle(chartStyle) {
        Chart(
            chart = lineChart(),
            model = entryModel,
            modifier = modifier,
            chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false)
        )
    }
}
