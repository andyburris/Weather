package com.andb.apps.weather.ui.settings

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.andb.apps.weather.R
import com.andb.apps.weather.util.and
import com.andb.apps.weather.util.dp
import com.andb.apps.weather.util.sp
import kotlinx.android.synthetic.main.hour_picker_view.view.*
import kotlin.math.*

class HourPicker : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private val onSelectListeners: MutableList<(hour: Int) -> Unit> = mutableListOf()
    private var meridiem = Meridiem.AM
        set(value) {
            field = value
            when (value) {
                Meridiem.AM -> {
                    hourPickerAM.alpha = 1f
                    hourPickerPM.alpha = .6f
                    (hourPickerAM and hourPickerPM).forEach { it.visibility = View.VISIBLE }
                }
                Meridiem.PM -> {
                    hourPickerPM.alpha = 1f
                    hourPickerAM.alpha = .6f
                    (hourPickerAM and hourPickerPM).forEach { it.visibility = View.VISIBLE }
                }
                else -> (hourPickerAM and hourPickerPM).forEach { it.visibility = View.GONE }
            }
        }
    var is24Hour
        get() = hourPickerRadialView.is24HourTime
        set(value) {
            hourPickerRadialView?.is24HourTime = value
            meridiem = when {
                value -> Meridiem.NONE
                hourPickerRadialView.selectedHour > 11 -> Meridiem.PM
                else -> Meridiem.AM
            }
        }
    var selectedHour
        get() = if (!is24Hour && hourPickerRadialView.selectedHour <= 11 && meridiem == Meridiem.PM) hourPickerRadialView.selectedHour + 12 else hourPickerRadialView.selectedHour
        set(value) {
            hourPickerRadialView.selectedHour = value
            if (!is24Hour) {
                meridiem = if (value / 12 > 0) Meridiem.PM else Meridiem.AM
            }
        }

    init {
        inflate(context, R.layout.hour_picker_view, this)
        hourPickerRadialView.onSelectListener = { hour ->
            hourPickerTime.text = "${if (is24Hour) hour else hour % 12}:00"
            onSelectListeners.forEach { it.invoke(hour) }
        }
        hourPickerTime.text = "${hourPickerRadialView.selectedHour}:00"
        hourPickerAM.setOnClickListener {
            it.alpha = 1f
            hourPickerPM.alpha = .7f
            meridiem = Meridiem.AM
        }
        hourPickerPM.setOnClickListener {
            meridiem = Meridiem.PM
        }
    }

    fun addOnSelectListener(listener: (hour: Int) -> Unit) {
        onSelectListeners.add(listener)
    }

}

/**
 * Radial view that only holds hour values. Supports 12 and 24 hr formats
 */
class HourPickerRadialView : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private val backgroundPaint = Paint().also {
        it.color = Color.LTGRAY
        it.isAntiAlias = true
    }

    private val unselectedTextPaint = TextPaint().also {
        it.textSize = 16.sp
        it.color = Color.BLACK
        it.isAntiAlias = true
    }

    private val selectedTextPaint = TextPaint().also {
        it.textSize = 16.sp
        it.color = Color.WHITE
        it.isAntiAlias = true
    }

    private val selectorPaint = Paint().also {
        it.color = Color.DKGRAY
        it.isAntiAlias = true
        it.strokeWidth = 2.dp.toFloat()
    }


    var clockBackgroundColor: Int
        get() = backgroundPaint.color
        set(value) {
            backgroundPaint.color = value
            invalidate()
        }

    var textColor: Int
        get() = unselectedTextPaint.color
        set(value) {
            unselectedTextPaint.color = value
            invalidate()
        }

    var onSelectListener: ((Int) -> Unit)? = null

    /** Hour from 0-23 that is selected (preserves numbers >11 even in 12hr mode until user input) */
    var selectedHour = 6
        set(value) {
            field = value
            onSelectListener?.invoke(field)
            invalidate()
        }

    var is24HourTime = false
        set(value) {
            field = value
            invalidate()
        }

    /** selectedHour in 24hr time if needed, otherwise mapped down to 12hr time. Only needed for radial positioning */
    private fun formattedHour() = if (is24HourTime) selectedHour else selectedHour % 12

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                Log.d("hourPicker", "Action was DOWN")
                val x = event.x - measuredWidth / 2
                val y = event.y - measuredHeight / 2
                val touchHyp = hypot(x, y)
                val viewHyp = measuredWidth / 2.0
                Log.d("hourPicker", "touchHyp: $touchHyp, viewHyp: $viewHyp")
                val touchingAM = when {
                    touchHyp > viewHyp - 32.dp -> true
                    touchHyp > viewHyp - 64.dp && is24HourTime -> false
                    else -> return@onTouchEvent true
                }
                val angle = atan((y / x).toDouble()) + if (x < 0) PI else 0.0
                val hourDecimal = (Math.toDegrees(angle) + 90) / 30
                val hour = hourDecimal.roundToInt() % 12 + if (!touchingAM) 12 else 0
                Log.d("hourPicker", "onTouchEvent: hourDecimal = $hourDecimal, hour = $hour")
                if (hour != selectedHour) {
                    selectedHour = hour
                }
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d("hourlyView", "onDraw: selectedHour = $selectedHour")
        canvas.drawCircle(
            (measuredWidth / 2).toFloat(),
            (measuredHeight / 2).toFloat(),
            (measuredWidth / 2).toFloat(),
            backgroundPaint
        )
        drawSelector(canvas)
        drawNumbers(canvas)
    }

    private fun drawSelector(canvas: Canvas) {
        val center = Coordinate((measuredWidth / 2).toFloat(), (measuredHeight / 2).toFloat())
        val end = getPositionOf(formattedHour())
        canvas.drawCircle(center, 4.dp.toFloat(), selectorPaint)
        canvas.drawCircle(end, (if (formattedHour() > 11) 16 else 20).dp.toFloat(), selectorPaint)
        canvas.drawLine(center, end, selectorPaint)
    }

    private fun drawNumbers(canvas: Canvas) {
        for (i in 0..11) {
            val text = if (i == 0 && !is24HourTime) "12" else i.toString()
            val paint = if (i == formattedHour()) selectedTextPaint else unselectedTextPaint
            val position = getPositionOf(i)
            val offset = Coordinate(-paint.measureText(text) / 2, paint.fontMetrics.ascent * -0.4f)
            canvas.drawText(text, position + offset, paint)
        }

        if (is24HourTime) {
            for (i in 12..23) {
                val text = i.toString()
                val paint = if (i == formattedHour()) selectedTextPaint else unselectedTextPaint
                val position = getPositionOf(i)
                val offset =
                    Coordinate(-paint.measureText(text) / 2, paint.fontMetrics.ascent * -0.4f)
                canvas.drawText(text, position + offset, paint)
            }
        }
    }

    private fun getPositionOf(
        hour: Int,
        offset: Int = if (hour >= 12) 56.dp else 24.dp
    ): Coordinate {
        val angle: Double = ANGLES[hour]
        val x = (measuredWidth / 2) + cos(angle) * (measuredWidth / 2 - offset)
        val y = (measuredWidth / 2) + sin(angle) * (measuredWidth / 2 - offset)
        return Coordinate(x.toFloat(), y.toFloat())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        // If one of the measures is match_parent, use that one to determine the size.
        // If not, use the default implementation of onMeasure.
        if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
            setMeasuredDimension(widthSize, widthSize)
        } else if (heightMode == MeasureSpec.EXACTLY && widthMode != MeasureSpec.EXACTLY) {
            setMeasuredDimension(heightSize, heightSize)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    companion object {
        private val ANGLES = (0..23).map { Math.toRadians(it * 30 - 90.0) }
    }
}

private class Coordinate(val x: Float, val y: Float) {
    operator fun plus(other: Coordinate) = Coordinate(x + other.x, y + other.y)
    operator fun minus(other: Coordinate) = Coordinate(x - other.x, y - other.y)
}

enum class Meridiem {
    AM, PM, NONE
}

private fun Canvas.drawText(text: String, coordinate: Coordinate, paint: TextPaint) =
    drawText(text, coordinate.x, coordinate.y, paint)

private fun Canvas.drawCircle(coordinate: Coordinate, radius: Float, paint: Paint) =
    drawCircle(coordinate.x, coordinate.y, radius, paint)

private fun Canvas.drawLine(start: Coordinate, end: Coordinate, paint: Paint) =
    drawLine(start.x, start.y, end.x, end.y, paint)