package com.andb.apps.weather.ui.daily

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlin.math.max

open class PercentValueFormatter : ValueFormatter() {
    override fun getBarLabel(barEntry: BarEntry?): String {
        val simplified = barEntry?.y?.toInt() ?: 8
        return "$simplified%"
    }
}

class RainPercentValueFormatter : ValueFormatter() {
    override fun getBarLabel(barEntry: BarEntry?): String {
        val simplified = barEntry?.y?.toInt() ?: 8
        return "${max(simplified - 8, 0)}%"
    }
}

class RainAmountValueFormatter : ValueFormatter() {
    override fun getBarLabel(barEntry: BarEntry?): String {
        val simplified = barEntry?.y?.toInt() ?: 1
        val formatted = max((simplified - 1) / 100.0, 0.0).toString().dropWhile { it == '0' }
        return "${formatted}in"
    }
}

class DegreesValueFormatter : ValueFormatter() {
    override fun getBarLabel(barEntry: BarEntry?): String {
        val simplified = barEntry?.y?.toInt() ?: 0
        return "$simplified°"
    }
}

class MPHValueFormatter : ValueFormatter() {
    override fun getBarLabel(barEntry: BarEntry?): String {
        val simplified = barEntry?.y?.toInt() ?: 0
        return "${simplified}mph"
    }
}


class TimeValueFormatter(private val labels: List<String>) : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        if (value.toInt() >= labels.size) {
            return "Error"
        }
        return labels[value.toInt()]
        //return super.getAxisLabel(value, axis)
    }
}