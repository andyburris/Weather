package com.andb.apps.weather.chart

import android.os.Handler
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import com.andb.apps.weather.data.local.Prefs
import com.andb.apps.weather.util.colorBetween
import com.andb.apps.weather.util.dpToPx
import com.andb.apps.weather.util.leastCommonMultiple
import com.andb.apps.weather.util.removeRange
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import java.util.*
import kotlin.math.min

class DataSetChanger(
    private val chart: BarChart,
    var oldData: List<Entry>,
    var newData: List<Entry>
) {
    private var duration: Int = 300
    private var oldMax: Float = chart.axisLeft.axisMaximum
    private var oldMin: Float = chart.axisLeft.axisMinimum
    private var oldColors: List<Int> = chart.data.getDataSetByIndex(0).colors
    private var oldTextColors: List<Int> =
        (chart.data.getDataSetByIndex(0) as BarDataSet).valueColors
    var newColors = oldColors
    var newTextColors = oldTextColors
    var newMax: Int = newData.maxByOrNull { it.y }?.y?.toInt() ?: 100
    var newMin: Int = 0
    private var startTime: Long = 0
    var fps = 60
    private var timerHandler: Handler? = null
    var interpolator: Interpolator = AccelerateDecelerateInterpolator()

    fun run(animate: Boolean = true) {
        if (animate) {
            startTime = Calendar.getInstance().timeInMillis
            timerHandler = Handler()
            val runner = Runner()
            runner.run()
        } else {
            update(1f)
            finalize()
        }
    }

    private inner class Runner : Runnable {
        override fun run() {
            var increment = (Calendar.getInstance().timeInMillis - startTime) / duration.toFloat()
            increment = interpolator.getInterpolation(if (increment < 0f) 0f else if (increment > 1f) 1f else increment)

            update(increment)

            if (increment < 1f) {
                timerHandler!!.postDelayed(this, (1000 / fps).toLong())
            } else {
                finalize()
            }
        }
    }

    private fun update(increment: Float) {
        chart.apply {
            data.getDataSetByIndex(0).clear()

            for (i in newData.indices) {
                val oldY = if (oldData.size > i) oldData[i].y else newData[i].y
                val oldX = if (oldData.size > i) oldData[i].x else newData[i].x
                val newX = newData[i].x
                val newY = newData[i].y
                val e = BarEntry(
                    oldX + (newX - oldX) * increment,
                    oldY + (newY - oldY) * increment/*, newData[i].icon*/
                )
                data.addEntry(e, 0)
            }
            (data.getDataSetByIndex(0) as BarDataSet).apply {
                colors = colorDiffLists(oldColors, newColors, increment, entryCount)
            }
            axisLeft.axisMaximum = oldMax + (newMax - oldMax) * increment
            axisLeft.axisMinimum = oldMin + (newMin - oldMin) * increment
            xAxis.resetAxisMaximum()
            xAxis.resetAxisMinimum()
            notifyDataSetChanged()
            refreshDrawableState()
            invalidate()
            isAutoScaleMinMaxEnabled = true
        }

    }

    private fun finalize() {
        chart.xAxis.labelCount = newData.size
        (chart.data.getDataSetByIndex(0) as BarDataSet).colors =
            newColors //reset in case newColors has less colors than oldColors and colorDiffLists created inefficiency
        (chart.data.getDataSetByIndex(0) as BarDataSet).setValueTextColors(newTextColors)
        val newWidth = newData.size * dpToPx(Prefs.barWidth)
        chart.layoutParams = chart.layoutParams.also { it.width = newWidth }
        chart.invalidate()
    }

    private fun colorDiffLists(oldList: List<Int>, newList: List<Int>, increment: Float, totalEntries: Int): List<Int> {
        if (oldList.size == newList.size) {
            return colorDiffEqualLists(oldList, newList, increment)
        }

        val oldSize = oldList.size
        val newSize = newList.size

        val duplicatedOldList = oldList.toMutableList()
        val duplicatedNewList = newList.toMutableList()

        //Try to duplicate each list until they are the same size or larger than the amount of entries
        val lcm = leastCommonMultiple(oldSize, newSize)

        //the total size the lists should be
        val ceiling = min(lcm, totalEntries - 1)

        while (duplicatedOldList.size < ceiling) {
            duplicatedOldList.addAll(oldList)
        }
        duplicatedOldList.removeRange(ceiling, duplicatedOldList.size)

        while (duplicatedNewList.size < ceiling) {
            duplicatedNewList.addAll(newList)
        }
        duplicatedNewList.removeRange(ceiling, duplicatedNewList.size)

        return colorDiffEqualLists(duplicatedOldList, duplicatedNewList, increment)
    }

    private fun colorDiffEqualLists(oldList: List<Int>, newList: List<Int>, increment: Float): List<Int> {
        return oldList.mapIndexed { index, value ->
            colorBetween(
                value,
                newList[index],
                increment
            )
        }
    }

}