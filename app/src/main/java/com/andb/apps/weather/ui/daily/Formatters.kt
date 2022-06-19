package com.andb.apps.weather.ui.daily

import com.andb.apps.weather.data.local.Prefs
import com.andb.apps.weather.data.model.UnitType
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
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
        return "${formatted}${
            when (Prefs.units) {
                UnitType.US -> "in"
                UnitType.SI -> "cm"
            }
        }"
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


class TimeValueFormatter(private val labels: List<ZonedDateTime>) : ValueFormatter() {
    private val formatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern(if (Prefs.time24HrFormat) "H:mm" else "ha")

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        if (value.toInt() >= labels.size) {
            return "Error"
        }
        return labels[value.toInt()].format(formatter)
        //return super.getAxisLabel(value, axis)
    }
}