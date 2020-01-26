package com.andb.apps.weather.chart

import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.drawable.Drawable
import com.andb.apps.weather.data.local.Prefs
import com.andb.apps.weather.util.dp
import com.andb.apps.weather.util.dpToPx
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ViewPortHandler


class ImageBarChartRenderer(
    chart: BarDataProvider,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) :
    BarChartRoundedRenderer(chart, animator, viewPortHandler) {

    var images: List<Drawable> = listOf()

    override fun drawDataSet(c: Canvas, dataSet: IBarDataSet, index: Int) {
        super.drawDataSet(c, dataSet, index)
        val icons = mutableListOf<Drawable?>()
        for (i in 0 until dataSet.entryCount) {
            icons.add(i, dataSet.getEntryForIndex(i)?.icon)
        }
        //Log.d("drawDataSet", "icons: $icons")
    }

    override fun drawValue(c: Canvas?, valueText: String?, x: Float, y: Float, color: Int) {
        mValuePaint.color = color
        val index = (x / dpToPx(Prefs.barWidth)).toInt()
        //Log.d("drawValue", "index: $index")
        //Log.d("drawValue", "canvas - width: ${c?.width}, height: ${c?.height} | x: $x, y: $y")
        val icon = images.getOrNull(index)
        if (icon != null) {
            val textBounds = Rect()
            mValuePaint.getTextBounds(valueText ?: "", 0, valueText?.length ?: 1, textBounds)
            val textWidth = textBounds.width()
            val iconSize = 8.dp
            val left = x.toInt() - iconSize / 2 - textWidth / 2
            val top = y.toInt() - textBounds.height() / 2 - iconSize / 2
            icon.bounds = Rect(left, top, left + iconSize, top + iconSize)
            icon.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            if (c != null) {
                icon.draw(c)
            }
        }
        c?.drawText(valueText ?: "", x + (icon?.bounds?.width() ?: 0) / 2, y, mValuePaint)
    }
}