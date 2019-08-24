package com.andb.apps.weather

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import com.andb.apps.weather.chart.BarChartRoundedRenderer
import com.andb.apps.weather.chart.animateChange
import com.andb.apps.weather.objects.DailyConditions
import com.andb.apps.weather.objects.HourlyConditions
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import kotlinx.android.synthetic.main.daily_card.view.*
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
import java.util.*


class DailyForecastView : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    lateinit var summary: DailyConditions
    private var rainVals = listOf<Int>()
    private var tempVals = listOf<Int>()
    private var uvVals = listOf<Int>()
    private var windVals = listOf<Pair<Int, Int>>()
    private var labels = listOf("7AM", "8AM", "9AM", "10AM", "11AM", "12PM", "1PM", "2PM", "3PM", "4PM", "5PM", "6PM", "7PM", "8PM", "9PM", "10PM", "11PM", "12AM")

    init {
        inflate(context, R.layout.daily_card, this)
        setupChart()
    }

    fun setupData(dailyData: DailyConditions, hourlyData: List<HourlyConditions>, timeZone: ZoneOffset) {
        summary = dailyData
        rainVals = hourlyData.map { (it.precipProbability * 100).toInt() }
        tempVals = hourlyData.map { it.temperature.toInt() }
        uvVals = hourlyData.map { it.uvIndex }
        windVals = hourlyData.map { Pair(it.windSpeed.toInt(), it.windBearing) }
        val formatter = DateTimeFormatter.ofPattern("ha")//TODO: 24hr option ("H")
        labels = hourlyData.map {
            it.time.atOffset(timeZone).format(formatter)
        }


        Log.d("setupData", "summary = $summary")
        Log.d("setupData", "rainVals = $rainVals")
        Log.d("setupData", "tempVals = $tempVals")
        Log.d("setupData", "uvVals = $uvVals")
        Log.d("setupData", "windVals = $windVals")
        Log.d("setupData", "times = ${hourlyData.map { it.time }}")
        Log.d("setupData", "labels = $labels")


        val dayOfWeek = dailyData.time.atOffset(timeZone)
            .dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        dailyName.text = dayOfWeek
        dailyHighLow.text = String.format(resources.getString(R.string.high_low_placeholder), dailyData.temperatureLow.toInt(), dailyData.temperatureHigh.toInt())
        dailyDescription.text = dailyData.summary


        populateChart(getDataSet(tempVals).also { it.colors = listOf(colors[0].first) },
            max = (tempVals.maxBy { it } ?: 0) + 5,
            min = (tempVals.minBy { it } ?: 0) - 10
        )
        setTemperature()

        dailyChipTemperature.setOnClickListener {
            setTemperature()
        }

        dailyChipRain.setOnClickListener {
            setRain()
        }

        dailyChipUV.setOnClickListener {
            setUVIndex()
        }

        dailyChipWind.setOnClickListener {
            setWind()
        }

    }

    //populateChartAnim(getList(rainVals), valueFormatter = PercentValueFormatter(), max = 100)

    fun setTemperature() {
        dailyChart.data.getDataSetByIndex(0).valueFormatter = DegreesValueFormatter()
        dailyChart.animateChange(getList(tempVals)) {
            newMax = (tempVals.maxBy { it } ?: 0) + 5
            newMin = (tempVals.minBy { it } ?: 0) - 10
            newColors = listOf(colors[0].first)
        }
        selectChip(0)
    }

    fun setRain() {
        dailyChart.data.getDataSetByIndex(0).valueFormatter = PercentValueFormatter()
        dailyChart.animateChange(getList(rainVals)) {
            newMax = 100
            newColors = listOf(colors[1].first)
        }
        selectChip(1)
    }

    fun setUVIndex() {
        dailyChart.data.getDataSetByIndex(0).valueFormatter = DefaultValueFormatter(0)
        dailyChart.animateChange(getList(uvVals)) {
            newMax = 10
            newColors = listOf(colors[2].first)
        }
        selectChip(2)
    }

    fun setWind() {
        dailyChart.data.getDataSetByIndex(0).valueFormatter = MPHValueFormatter()
        dailyChart.animateChange(getList(windVals.map { it.first })) {
            newMax = (windVals.maxBy { it.first }?.first ?: 0) + 2
            newMin = (windVals.minBy { it.first }?.first ?: 0) - 2
            newColors = listOf(colors[3].first)
        }
        selectChip(3)
    }

    private val chipList = listOf(dailyChipTemperature, dailyChipRain, dailyChipUV, dailyChipWind)
    private val colors = listOf(
        Pair(R.color.colorTemperatureBackground, R.color.colorTemperatureText),
        Pair(R.color.colorRainBackground, R.color.colorRainText),
        Pair(R.color.colorUVIndexBackground, R.color.colorUVIndexText),
        Pair(R.color.colorWindBackground, R.color.colorWindText)
    ).map { Pair(context.getColorCompat(it.first), context.getColorCompat(it.second)) }

    fun selectChip(index: Int) {
        val chip = chipList[index]
        chip.apply {
            isSelected = true
            setTextColor(colors[index].second)
            chipIconTint = ColorStateList.valueOf(colors[index].second)
        }
        chipList.minus(chip).forEach {
            it.isSelected = false
            val textIconColor = context.getColorCompat(R.color.chipDefault)
            it.setTextColor(textIconColor)
            it.chipIconTint = ColorStateList.valueOf(textIconColor)
        }
    }

    private fun setupChart() {
        dailyChart.apply {
            renderer = BarChartRoundedRenderer(this, animator, viewPortHandler)
                .also { it.radius = 8 }

            isDoubleTapToZoomEnabled = false
            setScaleEnabled(false)
            zoom(1.8f, 1f, 0f, 0f) //~36dp

            xAxis.apply {
                setDrawGridLines(false)
                granularity = 1f
                position = XAxis.XAxisPosition.BOTTOM
                axisLineWidth = 0f
                typeface = Typeface.DEFAULT_BOLD
                textSize = 10f
                setDrawAxisLine(false)
            }
            axisLeft.isEnabled = false
            axisRight.isEnabled = false

            description.text = ""
            legend.isEnabled = false

        }
    }

    private fun populateChart(dataset: BarDataSet, min: Int = 0, max: Int = dataset.yMax.toInt()) {
        dailyChart.apply {
            val newData = BarData(arrayListOf<IBarDataSet>(dataset)).also { it.barWidth = .9f }
            data = newData
            xAxis.labelCount = data.dataSetCount
            xAxis.valueFormatter = TimeValueFormatter(labels)
            axisLeft.axisMaximum = max.toFloat()
            axisLeft.axisMinimum = 0f
            invalidate()
        }

    }

    private fun getDataSet(values: Collection<Int>, formatter: ValueFormatter? = null): BarDataSet {
        val entries = values.mapIndexed { index, value -> BarEntry(index.toFloat(), value.toFloat()) }
        return BarDataSet(entries, "rainData").also {
            it.valueFormatter = formatter
            it.isHighlightEnabled = false
        }
    }

    private fun getList(values: Collection<Int>): List<BarEntry> {
        return values.mapIndexed { index, value -> BarEntry(index.toFloat(), value.toFloat()) }
    }
}

class PercentValueFormatter : ValueFormatter() {
    override fun getBarLabel(barEntry: BarEntry?): String {
        val simplified = barEntry?.y?.toInt() ?: 0
        return "$simplified%"
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