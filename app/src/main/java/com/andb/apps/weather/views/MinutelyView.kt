package com.andb.apps.weather.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.transition.TransitionManager
import com.andb.apps.weather.R
import com.andb.apps.weather.data.local.Prefs
import com.andb.apps.weather.data.model.Minutely
import com.andb.apps.weather.util.dp
import com.andb.apps.weather.util.dpToPx
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.minutely_layout.view.*
import kotlinx.android.synthetic.main.rain_marker_view.view.*
import org.threeten.bp.ZoneOffset
import kotlin.random.Random

class MinutelyView : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        inflate(context, R.layout.minutely_layout, this)
        setupChart()
        setupToggle()
    }

    var onToggle: ((collapsing: Boolean, oldHeight: Int, newHeight: Int, animate: Boolean) -> Unit)? =
        null
    var onTouch: (action: Int) -> Unit = {}

    private fun setupChart() {

        minutelyChart.apply {

            isDoubleTapToZoomEnabled = false
            setScaleEnabled(false)
            zoom(1f, 1f, 0f, 0f)

            xAxis.apply {
                setDrawGridLines(false)
                granularity = 60f / 3
                position = XAxis.XAxisPosition.BOTTOM
                axisLineWidth = 0f
                typeface = Typeface.DEFAULT_BOLD
                textSize = 10f
                textColor = ContextCompat.getColor(context, R.color.colorPrimary)
                setDrawAxisLine(false)
                valueFormatter = MinuteValueFormatter(context)
                setAvoidFirstLastClipping(true)
            }

            axisLeft.apply {
                setDrawGridLines(false)
                setLabelCount(3, true)
                axisMaximum = .4f
                axisMinimum = 0f
                textColor = ContextCompat.getColor(context, R.color.colorPrimary)
                setDrawAxisLine(false)
                valueFormatter = IntensityValueFormatter(context, axisMinimum, axisMaximum)
                addLimitLine(LimitLine(axisMaximum / 3).also {
                    it.enableDashedLine(8.dp.toFloat(), 8.dp.toFloat(), 0f)
                    it.lineColor =
                        ContextCompat.getColor(context, R.color.colorRainBackgroundDefault)
                })
                addLimitLine(LimitLine(axisMaximum / 3 * 2).also {
                    it.enableDashedLine(8.dp.toFloat(), 8.dp.toFloat(), 0f)
                    it.lineColor =
                        ContextCompat.getColor(context, R.color.colorRainBackgroundDefault)
                })
            }

            setViewPortOffsets(0f, viewPortHandler.offsetTop(), 0f, viewPortHandler.offsetBottom())

            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            marker = RainMarkerView(context)

            //remove highlight if not dragging
            setOnTouchListener { view, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    highlightValues(null)
                }
                onTouch.invoke(motionEvent.action)
                return@setOnTouchListener false
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun setup(minutely: Minutely?, offset: ZoneOffset?) {
        if (minutely != null && offset != null) {
            val values = minutely.data.mapIndexed { index, minutelyConditions ->
                Entry(index.toFloat(), minutelyConditions.precipIntensity.toFloat())
            }
            populateChart(values)
            minutelyTitle.apply {
                text = String.format(context.getString(R.string.next_hour), minutely.summary)

                val toCollapse = (values.maxBy { it.y }?.y ?: 0f) < 0.05f
                toggle(toCollapse, animate = true)

            }

        } else {
            populateChart(randomEntries())
        }
    }

    private fun populateChart(entries: List<Entry>) {

        val lds = LineDataSet(entries, "amounts").also {
            it.setDrawCircles(false)
            //it.color = ContextCompat.getColor(requireContext(), R.color.colorRainBackgroundDefault)
            it.cubicIntensity = .2f
            it.fillColor = ContextCompat.getColor(context, R.color.colorRainBackgroundDefault)
            it.fillAlpha = 255
            it.setDrawFilled(true)
            it.setFillFormatter { dataSet, dataProvider ->
                return@setFillFormatter minutelyChart.axisLeft.axisMinimum
            }
            it.setDrawValues(false)
            it.highlightLineWidth = dpToPx(2).toFloat()
            it.highLightColor = ContextCompat.getColor(context, R.color.colorRainTextDefault)
            it.setDrawHorizontalHighlightIndicator(false)
        }

        minutelyChart.data = LineData(arrayListOf<ILineDataSet>(lds))
    }

    var mCollapsed: Boolean = true
    private fun setupToggle() {
        minutelyCollapseTarget.setOnClickListener {
            toggle()
        }
    }

    fun toggle(toCollapse: Boolean = !mCollapsed, animate: Boolean = true) {
        Log.d("setupToggle", "toggled - collapse: $toCollapse")

        minutelyCollpaseIcon.animate().rotation(if (toCollapse) 180f else 0f).setDuration(300)
            .start()

        //val oldHeight = if (toCollapse) expandedHeight() else collapsedHeight()
        val oldHeight = minutelyHolder.height
        val newHeight = if (toCollapse) collapsedHeight() else expandedHeight()
        Log.d("setupToggle", "oldHeight: $oldHeight")
        Log.d("setupToggle", "newHeight: $newHeight")

        if (animate) {
            TransitionManager.beginDelayedTransition(minutelyHolder.rootView as ViewGroup)
        }
        if (toCollapse) {
            minutelyHolder.layoutParams =
                minutelyHolder.layoutParams.also { it.height = collapsedHeight() }
        } else {
            minutelyHolder.layoutParams =
                minutelyHolder.layoutParams.also { it.height = ViewGroup.LayoutParams.WRAP_CONTENT }
        }

        onToggle?.invoke(toCollapse, oldHeight, newHeight, animate)
        mCollapsed = toCollapse
    }

    fun collapsedHeight() = minutelyCollapseTarget.height

    //fun expandedHeight() = minutelyCollapseTarget.height + minutelyChartHolder.height + dpToPx(16)
    fun expandedHeight() = collapsedHeight() + dpToPx(120) + dpToPx(
        16
    )

    fun randomEntries(): List<Entry> {
        var amt = Random.nextFloat()
        val precipAmts = mutableListOf<Float>()
        for (i in 0..60) {
            val newAmt = (amt + (Random.nextFloat() / 5 * Random.nextSign())).coerceIn(0f, .4f)
            precipAmts.add(newAmt)
            amt = newAmt
        }

        return precipAmts.mapIndexed { index, fl -> Entry(index.toFloat(), fl) }
    }
}


private class MinuteValueFormatter(val context: Context) : ValueFormatter() {

    val minsPlaceholder = context.getString(R.string.minutes_placeholder)

/*    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        when(value==axis.ent)
        return super.getFormattedValue(value)
    }*/

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return String.format(minsPlaceholder, value.toInt())
    }
}

private class IntensityValueFormatter(val context: Context, val min: Float, val max: Float) :
    ValueFormatter() {

    val light = context.getString(R.string.precip_light)
    val med = context.getString(R.string.precip_med)
    val heavy = context.getString(R.string.precip_heavy)

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {

        val oneThird = min + (max - min) / 3
        val twoThirds = 2 * oneThird

        return when (value) {
            in min..(oneThird) -> light
            in oneThird..twoThirds -> med
            else -> heavy
        }
    }

}

private class RainMarkerView(context: Context) : MarkerView(context, R.layout.rain_marker_view) {

    val valueFormatter = MinuteValueFormatter(context)

    @SuppressLint("SetTextI18n")
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        rainMarkerTime.text = valueFormatter.getAxisLabel(e?.x ?: 0f, null)
        rainMarkerAmount.text = "${e?.y ?: 0} ${Prefs.rainUnit}"
    }

    override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {
        return MPPointF(-width / 4f, 0f)
    }

    override fun draw(canvas: Canvas?, posX: Float, posY: Float) {
        val maxX = (canvas?.width?.toFloat() ?: 0f) - width
        val newX = (posX + getOffsetForDrawingAtPoint(posX, posY).x).coerceIn(0f, maxX)
        val newY = 0f

        canvas?.translate(newX, newY)
        draw(canvas)
        canvas?.translate(-newX, -newY)
    }
}

private fun Random.nextSign(): Int {
    val seed = nextBoolean()
    return if (seed) -1 else 1
}