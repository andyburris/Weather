package com.andb.apps.weather.chart

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.data.Entry

fun BarChart.animateChange(newData: List<Entry>,
                           oldData: List<Entry> = getChartEntries(this),
                           animate: Boolean = true,
                           block: DataSetChanger.() -> Unit
) {
    val animator = DataSetChanger(this, oldData, newData)
    block.invoke(animator)
    animator.run(animate)
}

private fun getChartEntries(chart: Chart<*>): List<Entry> {
    return ArrayList<Entry>().also {
        val ds = chart.data.dataSets[0]
        for (i in 0 until ds.entryCount) {
            it.add(ds.getEntryForIndex(i))
        }
    }
}