package com.andb.apps.weather.ui.daily

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.andb.apps.weather.R
import com.andb.apps.weather.chart.ImageBarChartRenderer
import com.andb.apps.weather.data.local.Prefs
import com.andb.apps.weather.data.model.DailyConditions
import com.andb.apps.weather.data.model.HourlyConditions
import com.andb.apps.weather.ui.main.GraphConfig
import com.andb.apps.weather.util.colorByNightMode
import com.andb.apps.weather.util.dp
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import kotlinx.android.synthetic.main.daily_card.view.*
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.TextStyle
import java.util.*


class DailyForecastView : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    var syncScrollListener: ((oldPos: Int, newPos: Int) -> Unit)? = null
    fun anchorHour() = labels.first().hour

    private lateinit var summary: DailyConditions
    private var rainPercentVals = listOf<Int>()
    private var rainAmountVals = listOf<Int>()
    private var tempVals = listOf<Int>()
    private var feelsLikeVals = listOf<Int>()
    private var uvVals = listOf<Int>()
    private var windVals = listOf<Pair<Int, Int>>()
    private var labels = listOf<ZonedDateTime>()

    private lateinit var graphConfig: GraphConfig

    init {
        inflate(context, R.layout.daily_card, this)
    }

    fun setupData(
        chipIndex: Int,
        dailyData: DailyConditions,
        hourlyData: List<HourlyConditions>,
        graphConfig: GraphConfig
    ) {
        summary = dailyData
        rainPercentVals =
            hourlyData.map { (it.precipProbability * 100).toInt() + 8 } //add so zeroes are less ugly
        rainAmountVals =
            hourlyData.map { (it.precipIntensity * 100).toInt() + 1 } //add so zeroes are less ugly
        tempVals = hourlyData.map { it.temperature.toInt() }
        feelsLikeVals = hourlyData.map { it.apparentTemperature.toInt() }
        uvVals = hourlyData.map { it.uvIndex }
        windVals = hourlyData.map { Pair(it.windSpeed.toInt(), it.windDirection) }
        labels = hourlyData.map { it.time }
        this.graphConfig = graphConfig

        setupChart()

        Log.d("setupData", "summary = $summary")
        Log.d("setupData", "rainPercentVals = $rainPercentVals")
        Log.d("setupData", "tempVals = $tempVals")
        Log.d("setupData", "uvVals = $uvVals")
        Log.d("setupData", "windVals = $windVals")
        Log.d("setupData", "times = ${hourlyData.map { it.time }}")
        Log.d("setupData", "labels = $labels")


        val dayOfWeek = dailyData.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        dailyName.text =
            if (dailyData.date.dayOfMonth == LocalDateTime.now().dayOfMonth) resources.getString(R.string.today) else dayOfWeek
        dailyHighLow.text = String.format(
            resources.getString(R.string.high_low_placeholder),
            dailyData.temperatureLow.toInt(),
            dailyData.temperatureHigh.toInt()
        )
        dailyDescription.text = dailyData.summary
        dailyDescription.isVisible = dailyData.summary.isNotEmpty()

        dailyChart.data = initialData(hourlyData.size)
        dailyChart.data.getDataSetByIndex(0).apply {
            valueTextSize = 10f
        }
        changeDisplay(chipIndex, false)

        dailyDetailsRecycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = detailsAdapter(dailyData, context)
        }
    }

    fun changeDisplay(chipIndex: Int, animate: Boolean = true) {
        if (chipIndex == 0) setDetails() else setGraph()
        when (chipIndex) {
            1 -> dailyChart.setTemperature(
                tempVals,
                animate,
                graphConfig.tempMin.toInt(),
                graphConfig.tempMax.toInt()
            )
            2 -> dailyChart.setRainPercent(rainPercentVals, animate)
            3 -> dailyChart.setUVIndex(uvVals, animate)
            4 -> dailyChart.setWind(windVals, animate, graphConfig.windMax.toInt())
            5 -> dailyChart.setFeelsLike(feelsLikeVals, animate)
            6 -> dailyChart.setRainAmount(
                rainAmountVals,
                animate,
                (graphConfig.rainMax * 100).toInt()
            )
        }
    }

    private fun setDetails() {
        dailyHorizontalScrollView.visibility = View.INVISIBLE
        dailyDetailsRecycler.visibility = View.VISIBLE
    }

    private fun setGraph() {
        dailyHorizontalScrollView.visibility = View.VISIBLE
        dailyChart.setOnLongClickListener {
            Log.d("dailyChart", "long click")
            dailyHorizontalScrollView.onScrollChanged = { x, y, oldX, oldY ->
                syncScrollListener?.invoke(oldX, x)
            }
            dailyHorizontalScrollView.onScrollEnd = {
                dailyHorizontalScrollView.onScrollChanged = null
            }
            true
        }
        dailyDetailsRecycler.visibility = View.INVISIBLE
    }

    fun refreshColors(selected: Int) {
        changeDisplay(selected)
        if (selected == 0) {
            dailyDetailsRecycler.adapter?.notifyDataSetChanged()
        }
    }

    fun syncScroll(sourcePos: Int, anchorHour: Int) {
        val offset = anchorHour - this.anchorHour()
        val adjustedPos = sourcePos + offset * (Prefs.barWidth.dp)
        dailyHorizontalScrollView.scrollTo(adjustedPos.coerceAtLeast(0), 0)
    }

    private fun setupChart() {
        dailyChart.apply {
            renderer = ImageBarChartRenderer(this, animator, viewPortHandler)
                .also { it.radius = 8 }

            isDoubleTapToZoomEnabled = false
            setScaleEnabled(false)
            zoom(1f, 1f, 0f, 0f) //~36dp

            xAxis.apply {
                setDrawGridLines(false)
                granularity = 1f
                position = XAxis.XAxisPosition.BOTTOM
                axisLineWidth = 0f
                typeface = Typeface.DEFAULT_BOLD
                textColor = resources.colorByNightMode(Color.BLACK, Color.WHITE)
                textSize = 10f
                setDrawAxisLine(false)
                valueFormatter = TimeValueFormatter(labels)
            }
            axisLeft.isEnabled = false
            axisRight.isEnabled = false

            setViewPortOffsets(0f, viewPortHandler.offsetTop(), 0f, viewPortHandler.offsetBottom())

            description.text = ""
            legend.isEnabled = false

            invalidate()
        }
    }


    private fun initialData(size: Int): BarData {
        val list = mutableListOf<BarEntry>().also {
            for (i in 0 until size) {
                it.add(BarEntry(i.toFloat(), 0f))
            }
        }
        val set = BarDataSet(list, "data").also { it.isHighlightEnabled = false }
        return BarData(arrayListOf<IBarDataSet>(set)).also {
            it.barWidth = 1f - 4f / Prefs.barWidth
        } //4dp between each bar
    }


}


