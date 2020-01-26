package com.andb.apps.weather.ui.daily

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.RotateDrawable
import com.andb.apps.weather.R
import com.andb.apps.weather.chart.ImageBarChartRenderer
import com.andb.apps.weather.chart.animateChange
import com.andb.apps.weather.data.local.Prefs
import com.andb.apps.weather.util.colorByNightMode
import com.andb.apps.weather.util.mapRange
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.DefaultValueFormatter

private val COLORS
    get() = listOf(Prefs.colorTemperature, Prefs.colorRain, Prefs.colorUVIndex, Prefs.colorWind)

fun BarChart.setTemperature(values: List<Int>, animate: Boolean) {
    data.getDataSetByIndex(0).valueFormatter = DegreesValueFormatter()
    (renderer as ImageBarChartRenderer).images = listOf()
    animateChange(asEntries(values), animate = animate) {
        newMax = (values.maxBy { it } ?: 0) + 5
        newMin = (values.minBy { it } ?: 0) - 10
        newColors = listOf(COLORS[0])
        newTextColors = listOf(resources.colorByNightMode(Color.BLACK, Color.WHITE))
    }
}

fun BarChart.setFeelsLike(values: List<Int>, animate: Boolean) {
    data.getDataSetByIndex(0).valueFormatter = DegreesValueFormatter()
    (renderer as ImageBarChartRenderer).images = listOf()
    animateChange(asEntries(values), animate = animate) {
        newMax = (values.maxBy { it } ?: 0) + 5
        newMin = (values.minBy { it } ?: 0) - 10
        newColors = listOf(COLORS[0])
        newTextColors = listOf(resources.colorByNightMode(Color.BLACK, Color.WHITE))
    }
}


fun BarChart.setRainPercent(values: List<Int>, animate: Boolean) {
    data.getDataSetByIndex(0).valueFormatter = RainPercentValueFormatter()
    (renderer as ImageBarChartRenderer).images = listOf()
    animateChange(asEntries(values), animate = animate) {
        newMax = 100 + 8
        newColors = listOf(COLORS[1])
        newTextColors = listOf(resources.colorByNightMode(Color.BLACK, Color.WHITE))
    }
}

fun BarChart.setRainAmount(values: List<Int>, animate: Boolean) {
    data.getDataSetByIndex(0).valueFormatter =
        RainAmountValueFormatter()
    (renderer as ImageBarChartRenderer).images = listOf()
    animateChange(asEntries(values), animate = animate) {
        newMax = (values.maxBy { it } ?: 0) + 1
        newColors = listOf(COLORS[1])
        newTextColors = listOf(resources.colorByNightMode(Color.BLACK, Color.WHITE))
    }
}


fun BarChart.setUVIndex(values: List<Int>, animate: Boolean) {
    data.getDataSetByIndex(0).valueFormatter = DefaultValueFormatter(0)
    (renderer as ImageBarChartRenderer).images = listOf()
    animateChange(asEntries(values), animate = animate) {
        newMax = 10
        newColors = listOf(COLORS[2])
        newTextColors = listOf(resources.colorByNightMode(Color.BLACK, Color.WHITE))
    }
}

fun BarChart.setWind(values: List<Pair<Int, Int>>, animate: Boolean) {
    val dataSet = data.getDataSetByIndex(0)
    dataSet.valueFormatter = MPHValueFormatter()
    val entries = getListWind(values.map { it.first }, values.map { it.second }, context)
    (renderer as ImageBarChartRenderer).images = entries.map { it.icon }
    animateChange(entries, animate = animate) {
        newMax = (values.maxBy { it.first }?.first ?: 0) + 2
        newMin = (values.minBy { it.first }?.first ?: 0) - 2
        newColors = listOf(COLORS[3])
        newTextColors = listOf(resources.colorByNightMode(Color.BLACK, Color.WHITE))
    }
}

private fun asEntries(values: Collection<Int>): List<BarEntry> {
    return values.mapIndexed { index, value -> BarEntry(index.toFloat(), value.toFloat()) }
}


private fun getListWind(mph: List<Int>, directions: List<Int>, context: Context): List<BarEntry> {
    val icon = RotateDrawable()
    icon.drawable = context.getDrawable(R.drawable.ic_arrow_n)?.mutate()
    return mph.mapIndexed { index, value ->
        BarEntry(index.toFloat(), value.toFloat(), icon.also {
            it.fromDegrees = 0f
            it.toDegrees = 360f
            it.level = directions[index].mapRange(0..360, 0..10000)
        })
    }
}