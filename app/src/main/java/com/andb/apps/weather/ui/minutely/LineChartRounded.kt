package com.andb.apps.weather.ui.minutely

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import com.github.mikephil.charting.charts.LineChart

class LineChartRounded(context: Context, attrs: AttributeSet?) : LineChart(context, attrs) {

    var gridBackgroundRadiusTopLeft = 0f
    var gridBackgroundRadiusTopRight = 0f
    var gridBackgroundRadiusBottomLeft = 0f
    var gridBackgroundRadiusBottomRight = 0f

    fun updateGridBackgroundRadius(
        topLeft: Float = gridBackgroundRadiusTopLeft,
        topRight: Float = gridBackgroundRadiusTopRight,
        bottomLeft: Float = gridBackgroundRadiusBottomLeft,
        bottomRight: Float = gridBackgroundRadiusBottomRight
    ) {
        gridBackgroundRadiusTopLeft = topLeft
        gridBackgroundRadiusTopRight = topRight
        gridBackgroundRadiusBottomLeft = bottomLeft
        gridBackgroundRadiusBottomRight = bottomRight
    }

    fun updateGridBackgroundRadius(radius: Float) {
        gridBackgroundRadiusTopLeft = radius
        gridBackgroundRadiusTopRight = radius
        gridBackgroundRadiusBottomLeft = radius
        gridBackgroundRadiusBottomRight = radius
    }

    override fun drawGridBackground(c: Canvas) {
        if (mDrawGridBackground) {
            val corners = floatArrayOf(
                gridBackgroundRadiusTopLeft, gridBackgroundRadiusTopLeft,
                gridBackgroundRadiusTopRight, gridBackgroundRadiusTopRight,
                gridBackgroundRadiusBottomRight, gridBackgroundRadiusBottomRight,
                gridBackgroundRadiusBottomLeft, gridBackgroundRadiusBottomLeft
            )
            val roundedRect = Path().apply {
                addRoundRect(
                    mViewPortHandler.contentRect,
                    corners,
                    Path.Direction.CW
                )
            }
            c.drawPath(roundedRect, mGridBackgroundPaint)
        }

        if (mDrawBorders) {
            c.drawRect(mViewPortHandler.contentRect, mBorderPaint)
        }
    }
}