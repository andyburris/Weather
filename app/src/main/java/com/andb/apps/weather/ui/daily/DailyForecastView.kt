package com.andb.apps.weather.ui.daily

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.RotateDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.andb.apps.weather.R
import com.andb.apps.weather.chart.ImageBarChartRenderer
import com.andb.apps.weather.chart.animateChange
import com.andb.apps.weather.data.local.Prefs
import com.andb.apps.weather.data.model.DailyConditions
import com.andb.apps.weather.data.model.HourlyConditions
import com.andb.apps.weather.util.chipTextFrom
import com.andb.apps.weather.util.colorByNightMode
import com.andb.apps.weather.util.mapRange
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.rongi.klaster.Klaster
import kotlinx.android.synthetic.main.daily_card.view.*
import kotlinx.android.synthetic.main.details_item.view.*
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
import java.util.*


class DailyForecastView : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    lateinit var summary: DailyConditions
    private var rainPercentVals = listOf<Int>()
    private var rainAmountVals = listOf<Int>()
    private var tempVals = listOf<Int>()
    private var feelsLikeVals = listOf<Int>()
    private var uvVals = listOf<Int>()
    private var windVals = listOf<Pair<Int, Int>>()
    private var labels = listOf<String>()
    //private var labels = listOf("7AM", "8AM", "9AM", "10AM", "11AM", "12PM", "1PM", "2PM", "3PM", "4PM", "5PM", "6PM", "7PM", "8PM", "9PM", "10PM", "11PM", "12AM")

    init {
        inflate(context, R.layout.daily_card, this)
    }

    fun setupData(
        chipIndex: Int,
        dailyData: DailyConditions,
        hourlyData: List<HourlyConditions>,
        timeZone: ZoneOffset
    ) {
        summary = dailyData
        rainPercentVals =
            hourlyData.map { (it.precipProbability * 100).toInt() + 8 } //add so zeroes are less ugly
        rainAmountVals =
            hourlyData.map { (it.precipIntensity * 100).toInt() + 1 } //add so zeroes are less ugly
        tempVals = hourlyData.map { it.temperature.toInt() }
        feelsLikeVals = hourlyData.map { it.apparentTemperature.toInt() }
        uvVals = hourlyData.map { it.uvIndex }
        windVals = hourlyData.map { Pair(it.windSpeed.toInt(), it.windBearing) }
        val formatter = DateTimeFormatter.ofPattern("ha")//TODO: 24hr option ("H")
        labels = hourlyData.map {
            it.time.atOffset(timeZone).format(formatter)
        }

        setupChart()

        Log.d("setupData", "summary = $summary")
        Log.d("setupData", "rainPercentVals = $rainPercentVals")
        Log.d("setupData", "tempVals = $tempVals")
        Log.d("setupData", "uvVals = $uvVals")
        Log.d("setupData", "windVals = $windVals")
        Log.d("setupData", "times = ${hourlyData.map { it.time }}")
        Log.d("setupData", "labels = $labels")


        val dayOfWeek = dailyData.time.atOffset(timeZone)
            .dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        dailyName.text =
            if (dailyData.time.dayOfMonth == LocalDateTime.now().dayOfMonth) resources.getString(R.string.today) else dayOfWeek
        dailyHighLow.text = String.format(
            resources.getString(R.string.high_low_placeholder),
            dailyData.temperatureLow.toInt(),
            dailyData.temperatureHigh.toInt()
        )
        dailyDescription.text = dailyData.summary

        dailyChart.data = initialData(hourlyData.size)
        dailyChart.data.getDataSetByIndex(0).apply {
            valueTextSize = 10f
        }
        changeDisplay(chipIndex, false)

        dailyDetailsRecycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = detailsAdapter(dailyData)
        }
    }

    fun changeDisplay(chipIndex: Int, animate: Boolean = true) {
        when (chipIndex) {
            0 -> setDetails()
            1 -> setTemperature(animate)
            2 -> setRainPercent(animate)
            3 -> setUVIndex(animate)
            4 -> setWind(animate)
            5 -> setFeelsLike(animate)
            6 -> setRainAmount(animate)
        }
    }

    private fun setDetails() {
        dailyHorizontalScrollView.visibility = View.INVISIBLE
        dailyDetailsRecycler.visibility = View.VISIBLE
    }

    private fun setGraph() {
        dailyHorizontalScrollView.visibility = View.VISIBLE
        dailyDetailsRecycler.visibility = View.INVISIBLE
    }

    private fun setTemperature(animate: Boolean) {
        setGraph()
        dailyChart.data.getDataSetByIndex(0).valueFormatter =
            DegreesValueFormatter()
        (dailyChart.renderer as ImageBarChartRenderer).images = listOf()
        dailyChart.animateChange(getList(tempVals), animate = animate) {
            newMax = (tempVals.maxBy { it } ?: 0) + 5
            newMin = (tempVals.minBy { it } ?: 0) - 10
            newColors = listOf(colors[0])
            newTextColors = listOf(resources.colorByNightMode(Color.BLACK, Color.WHITE))
        }
    }

    private fun setFeelsLike(animate: Boolean) {
        setGraph()
        dailyChart.data.getDataSetByIndex(0).valueFormatter =
            DegreesValueFormatter()
        (dailyChart.renderer as ImageBarChartRenderer).images = listOf()
        dailyChart.animateChange(getList(feelsLikeVals), animate = animate) {
            newMax = (feelsLikeVals.maxBy { it } ?: 0) + 5
            newMin = (feelsLikeVals.minBy { it } ?: 0) - 10
            newColors = listOf(colors[0])
            newTextColors = listOf(resources.colorByNightMode(Color.BLACK, Color.WHITE))
        }
    }


    private fun setRainPercent(animate: Boolean) {
        setGraph()
        dailyChart.data.getDataSetByIndex(0).valueFormatter =
            RainPercentValueFormatter()
        (dailyChart.renderer as ImageBarChartRenderer).images = listOf()
        dailyChart.animateChange(getList(rainPercentVals), animate = animate) {
            newMax = 100 + 8
            newColors = listOf(colors[1])
            newTextColors = listOf(resources.colorByNightMode(Color.BLACK, Color.WHITE))
        }
    }

    private fun setRainAmount(animate: Boolean) {
        setGraph()
        dailyChart.data.getDataSetByIndex(0).valueFormatter =
            RainAmountValueFormatter()
        (dailyChart.renderer as ImageBarChartRenderer).images = listOf()
        dailyChart.animateChange(getList(rainAmountVals), animate = animate) {
            newMax = (rainAmountVals.maxBy { it } ?: 0) + 1
            newColors = listOf(colors[1])
            newTextColors = listOf(resources.colorByNightMode(Color.BLACK, Color.WHITE))
        }
    }


    private fun setUVIndex(animate: Boolean) {
        setGraph()
        dailyChart.data.getDataSetByIndex(0).valueFormatter = DefaultValueFormatter(0)
        (dailyChart.renderer as ImageBarChartRenderer).images = listOf()
        dailyChart.animateChange(getList(uvVals), animate = animate) {
            newMax = 10
            newColors = listOf(colors[2])
            newTextColors = listOf(resources.colorByNightMode(Color.BLACK, Color.WHITE))
        }
    }

    private fun setWind(animate: Boolean) {
        setGraph()
        val dataSet = dailyChart.data.getDataSetByIndex(0)
        dataSet.valueFormatter = MPHValueFormatter()
        val entries = getListWind(windVals.map { it.first }, windVals.map { it.second })
        (dailyChart.renderer as ImageBarChartRenderer).images = entries.map { it.icon }
        dailyChart.animateChange(entries, animate = animate) {
            newMax = (windVals.maxBy { it.first }?.first ?: 0) + 2
            newMin = (windVals.minBy { it.first }?.first ?: 0) - 2
            newColors = listOf(colors[3])
            newTextColors = listOf(resources.colorByNightMode(Color.BLACK, Color.WHITE))
        }
    }

    private var colors =
        listOf(Prefs.colorTemperature, Prefs.colorRain, Prefs.colorUVIndex, Prefs.colorWind)

    fun refreshColors(selected: Int) {
        colors =
            listOf(Prefs.colorTemperature, Prefs.colorRain, Prefs.colorUVIndex, Prefs.colorWind)
        changeDisplay(selected)
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

    private fun getList(values: Collection<Int>): List<BarEntry> {
        return values.mapIndexed { index, value -> BarEntry(index.toFloat(), value.toFloat()) }
    }

    private fun getListWind(mph: List<Int>, directions: List<Int>): List<BarEntry> {
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

    fun detailsAdapter(conditions: DailyConditions) = Klaster.get()
        .itemCount(6)
        .view(R.layout.details_item, LayoutInflater.from(context))
        .bind { pos ->
            val detailsItem: DetailsItem = when (pos) {
                //TODO: feels like
                0 -> DetailsItem(
                    R.string.rain,
                    String.format(
                        resources.getString(R.string.percent_placeholder),
                        (conditions.precipProbability * 100).toInt()
                    ),
                    R.drawable.ic_raindrop_black_24dp,
                    Prefs.colorRain
                )
                1 -> DetailsItem(
                    R.string.uv_index,
                    String.format(
                        resources.getString(R.string.max_placeholder),
                        conditions.uvIndex
                    ),
                    R.drawable.ic_uv_index_black_24dp,
                    Prefs.colorUVIndex
                )
                2 -> DetailsItem(
                    R.string.wind,
                    String.format(
                        resources.getString(R.string.max_placeholder_mph),
                        conditions.windSpeed.toInt()
                    ),
                    R.drawable.ic_wind_black_24dp,
                    Prefs.colorWind
                )
                3 -> {
                    val formatter = DateTimeFormatter.ofPattern("h:mma")
                    DetailsItem(
                        R.string.details_sunrise_sunset,
                        "${formatter.format(conditions.sunriseTime).toLowerCase()} | ${formatter.format(
                            conditions.sunsetTime
                        ).toLowerCase()}",
                        R.drawable.ic_weather_sunset,
                        Color.parseColor("#C4C4C4")
                    )
                }
                4 -> DetailsItem(
                    R.string.details_humidity,
                    String.format(
                        resources.getString(R.string.percent_placeholder),
                        (conditions.humidity * 100).toInt()
                    ),
                    R.drawable.ic_water_percent,
                    Color.parseColor("#C4C4C4")
                )
                else -> {
                    val moonPhaseIndex = when (conditions.moonPhase) {
                        in 0f..0.25f -> 0
                        in 0.25f..0.5f -> 1
                        in 0.5f..0.75f -> 2
                        else -> 3
                    }
                    val moonPhase =
                        resources.getStringArray(R.array.details_moon_states)[moonPhaseIndex]
                    DetailsItem(
                        R.string.details_moon,
                        moonPhase,
                        R.drawable.ic_weather_night,
                        Color.parseColor("#C4C4C4")
                    )
                }
            }

            itemView.apply {
                detailsItemTitle.setText(detailsItem.titleRes)
                detailsItemDescription.text = detailsItem.details
                detailsItemIcon.setImageResource(detailsItem.iconRes)
                detailsItemCircle.color = detailsItem.color
                detailsItemIcon.setColorFilter(chipTextFrom(detailsItem.color))
            }
        }
        .build()
}

private class DetailsItem(val titleRes: Int, val details: String, val iconRes: Int, val color: Int)

