package com.andb.apps.weather.ui.settings

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.andb.apps.weather.R

class CircleView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {


    private val paint = Paint().also {
        it.color = Color.BLACK
        it.isAntiAlias = true
    }

    var color: Int
        get() = paint.color
        set(value) {
            paint.color = value
            invalidate()
        }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CircleView, 0, 0)
        try {
            color = a.getInt(R.styleable.CircleView_color, -16777216) //black
        } finally {
            a.recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(
            (measuredWidth / 2).toFloat(),
            (measuredHeight / 2).toFloat(),
            (measuredWidth / 2).toFloat(),
            paint
        )
    }
}