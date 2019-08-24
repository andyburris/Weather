package com.andb.apps.weather.chart

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.data.Entry

fun BarChart.animateChange(newData: List<Entry>,
                           oldData: List<Entry> = getChartEntries(this),
                           block: AnimateDataSetChanged.() -> Unit) {
    val animator = AnimateDataSetChanged(this, oldData, newData)
    block.invoke(animator)
    animator.run()
}

private fun getChartEntries(chart: Chart<*>): List<Entry> {
    return ArrayList<Entry>().also {
        val ds = chart.data.dataSets[0]
        for (i in 0 until ds.entryCount) {
            it.add(ds.getEntryForIndex(i))
        }
    }
}