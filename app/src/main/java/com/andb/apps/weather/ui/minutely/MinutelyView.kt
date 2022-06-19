package com.andb.apps.weather.ui.minutely

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.transition.TransitionManager
import com.andb.apps.weather.R
import com.andb.apps.weather.data.local.Prefs
import com.andb.apps.weather.data.model.Minutely
import com.andb.apps.weather.data.model.UnitType
import com.andb.apps.weather.util.dp
import com.andb.apps.weather.util.dpToPx
import com.andb.apps.weather.util.getColorCompat
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

class MinutelyView(context: Context, attrs: AttributeSet? = null) :
    ConstraintLayout(context, attrs) {

    var onToggle: (heightChange: Int, animate: Boolean) -> Unit = { _, _ -> }
    var onTouch: (action: Int) -> Unit = {}

    private var mCollapsed: Boolean = true

    init {
        inflate(context, R.layout.minutely_layout, this)
        val a = context.obtainStyledAttributes(attrs, R.styleable.MinutelyView, 0, 0)

        try {
            val collapsed = a.getBoolean(R.styleable.MinutelyView_collapsed, true)
            toggle(collapsed)
        } finally {
            a.recycle()
        }

        setupChart()
        setupToggle()
    }

    private fun setupChart() {

        minutelyChart.apply {

            isDoubleTapToZoomEnabled = false
            setScaleEnabled(false)
            zoom(1f, 1f, 0f, 0f)

            xAxis.apply {
                setDrawGridLines(false)
                granularity = 60f / 3
                position = XAxis.XAxisPosition.BOTTOM
                typeface = Typeface.DEFAULT_BOLD
                textSize = 10f
                textColor = context.getColorCompat(R.color.colorPrimary)
                setDrawAxisLine(false)
                valueFormatter =
                    MinuteValueFormatter(
                        context
                    )
                setAvoidFirstLastClipping(true)
            }

            axisLeft.apply {
                setDrawGridLines(false)
                setLabelCount(3, true)
                axisMaximum = .4f
                axisMinimum = 0f
                textColor = context.getColorCompat(R.color.colorPrimary)
                setDrawAxisLine(false)
                valueFormatter =
                    IntensityValueFormatter(
                        context,
                        axisMinimum,
                        axisMaximum
                    )
                addLimitLine(LimitLine(axisMinimum).also {
                    it.lineColor = context.getColorCompat(R.color.colorPrimary)
                })
                addLimitLine(LimitLine(axisMaximum / 3).also {
                    it.enableDashedLine(2.dp.toFloat(), 4.dp.toFloat(), 0f)
                    it.lineColor = context.getColorCompat(R.color.colorPrimary)
                })
                addLimitLine(LimitLine(axisMaximum / 3 * 2).also {
                    it.enableDashedLine(2.dp.toFloat(), 4.dp.toFloat(), 0f)
                    it.lineColor = context.getColorCompat(R.color.colorPrimary)
                })
            }

            setViewPortOffsets(0f, 0f, 0f, viewPortHandler.offsetBottom())

            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
            setGridBackgroundColor(context.getColorCompat(R.color.minutelyGraphBackground))
            updateGridBackgroundRadius(8.dp.toFloat(), 8.dp.toFloat(), 0f, 0f)
            setDrawGridBackground(true)
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
    fun setup(minutely: Minutely) {
        val values = minutely.data.mapIndexed { index, minutelyConditions ->
            Entry(index.toFloat(), minutelyConditions.precipIntensity.toFloat())
        }
        populateChart(values)
        minutelyTitle.apply {
            text = minutely.summary
            val toCollapse = (values.maxByOrNull { it.y }?.y ?: 0f) < 0.05f
            toggle(toCollapse, animate = true)
        }
    }

    private fun populateChart(entries: List<Entry>) {

        val lds = LineDataSet(entries, "amounts").also {
            it.setDrawCircles(false)
            it.cubicIntensity = .2f
            it.fillColor = context.getColorCompat(R.color.colorRainBackgroundDefault)
            it.fillAlpha = 255
            it.setDrawFilled(true)
            it.setFillFormatter { dataSet, dataProvider ->
                return@setFillFormatter minutelyChart.axisLeft.axisMinimum
            }
            it.setDrawValues(false)
            it.highlightLineWidth = dpToPx(2).toFloat()
            it.highLightColor = context.getColorCompat(R.color.colorRainTextDefault)
            it.setDrawHorizontalHighlightIndicator(false)
        }

        minutelyChart.data = LineData(arrayListOf<ILineDataSet>(lds))
    }

    private fun setupToggle() {
        minutelyCollapseTarget.setOnClickListener {
            toggle()
        }
    }

    private fun toggle(toCollapse: Boolean = !mCollapsed, animate: Boolean = true) {
        Log.d("setupToggle", "toggled - collapse: $toCollapse")

        minutelyCollpaseIcon.animate()
            .rotation(if (toCollapse) 180f else 0f)
            .setDuration(300)
            .start()

        val oldHeight = if (toCollapse) expandedHeight() else collapsedHeight()
        val newHeight = if (toCollapse) collapsedHeight() else expandedHeight()
        Log.d("setupToggle", "oldHeight: $oldHeight")

        if (animate) {
            TransitionManager.beginDelayedTransition(minutelyHolder.rootView as ViewGroup)
        }

        minutelyHolder.updateLayoutParams {
            height = if (toCollapse) collapsedHeight() else ViewGroup.LayoutParams.WRAP_CONTENT
        }

        //val newHeight = minutelyHolder.height
        Log.d("setupToggle", "newHeight: $newHeight")

        onToggle.invoke(oldHeight - newHeight, animate)
        mCollapsed = toCollapse
    }

    private fun collapsedHeight() = minutelyCollapseTarget.height
    private fun expandedHeight() = collapsedHeight() + dpToPx(128) + dpToPx(16)

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

    val valueFormatter =
        MinuteValueFormatter(context)

    @SuppressLint("SetTextI18n")
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        rainMarkerTime.text = valueFormatter.getAxisLabel(e?.x ?: 0f, null)
        rainMarkerAmount.text = "${e?.y ?: 0} ${
            when (Prefs.units) {
                UnitType.US -> "in"
                UnitType.SI -> "cm"
            }
        }"
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