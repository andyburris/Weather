package com.andb.apps.weather.ui.daily

import android.graphics.Canvas
import com.andb.apps.weather.chart.BarChartRoundedRenderer
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler

open class BarChartPositiveLabelRenderer(
    chart: BarDataProvider,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : BarChartRoundedRenderer(chart, animator, viewPortHandler) {
    override fun drawValues(c: Canvas?) {
        drawValuesNew(c)
    }

    private fun drawValuesNew(c: Canvas?) {
        if (!isDrawingValuesAllowed(mChart)) return

        // if values are drawn
        val dataSets = mChart.barData.dataSets
        val valueOffsetPlus = Utils.convertDpToPixel(4.5f)
        val drawValueAboveBar = mChart.isDrawValueAboveBarEnabled
        for (i in 0 until mChart.barData.dataSetCount) {
            val dataSet = dataSets[i]
            if (!shouldDrawValues(dataSet)) continue

            // apply the text-styling defined by the DataSet
            applyValueTextStyle(dataSet)
            val isInverted = mChart.isInverted(dataSet.axisDependency)

            // calculate the correct offset depending on the draw position of the value
            val valueTextHeight = Utils.calcTextHeight(mValuePaint, "8").toFloat()
            var offset =
                if (drawValueAboveBar) -valueOffsetPlus else valueTextHeight + valueOffsetPlus

            // get the buffer
            val buffer = mBarBuffers[i]
            val phaseY = mAnimator.phaseY
            val formatter = dataSet.valueFormatter
            val iconsOffset = MPPointF.getInstance(dataSet.iconsOffset)
            iconsOffset.x = Utils.convertDpToPixel(iconsOffset.x)
            iconsOffset.y = Utils.convertDpToPixel(iconsOffset.y)

            // if only single values are drawn (sum)
            if (!dataSet.isStacked) {
                var j = 0
                while (j < buffer.buffer.size * mAnimator.phaseX) {
                    val x = (buffer.buffer[j] + buffer.buffer[j + 2]) / 2f
                    if (!mViewPortHandler.isInBoundsRight(x)) break
                    if (!mViewPortHandler.isInBoundsY(buffer.buffer[j + 1])
                        || !mViewPortHandler.isInBoundsLeft(x)
                    ) {
                        j += 4
                        continue
                    }
                    val entry = dataSet.getEntryForIndex(j / 4)
                    val `val` = entry.y
                    if (dataSet.isDrawValuesEnabled) {
                        drawValue(
                            c,
                            formatter.getBarLabel(entry),
                            x,
                            buffer.buffer[j + 1] + offset,
                            dataSet.getValueTextColor(j / 4)
                        )
                    }
                    if (entry.icon != null && dataSet.isDrawIconsEnabled) {
                        val icon = entry.icon
                        var px = x
                        var py = buffer.buffer[j + 1] + offset
                        px += iconsOffset.x
                        py += iconsOffset.y
                        Utils.drawImage(
                            c,
                            icon,
                            px.toInt(),
                            py.toInt(),
                            icon.intrinsicWidth,
                            icon.intrinsicHeight
                        )
                    }
                    j += 4
                }

                // if we have stacks
            } else {
                val trans =
                    mChart.getTransformer(dataSet.axisDependency)
                var bufferIndex = 0
                var index = 0
                while (index < dataSet.entryCount * mAnimator.phaseX) {
                    val entry = dataSet.getEntryForIndex(index)
                    val vals = entry.yVals
                    val x =
                        (buffer.buffer[bufferIndex] + buffer.buffer[bufferIndex + 2]) / 2f
                    val color = dataSet.getValueTextColor(index)

                    // we still draw stacked bars, but there is one
                    // non-stacked
                    // in between
                    if (vals == null) {
                        if (!mViewPortHandler.isInBoundsRight(x)) break
                        if (!mViewPortHandler.isInBoundsY(buffer.buffer[bufferIndex + 1])
                            || !mViewPortHandler.isInBoundsLeft(x)
                        ) continue
                        if (dataSet.isDrawValuesEnabled) {
                            drawValue(
                                c,
                                formatter.getBarLabel(entry),
                                x,
                                buffer.buffer[bufferIndex + 1] + offset,
                                color
                            )
                        }
                        if (entry.icon != null && dataSet.isDrawIconsEnabled) {
                            val icon = entry.icon
                            var px = x
                            var py = buffer.buffer[bufferIndex + 1] + offset
                            px += iconsOffset.x
                            py += iconsOffset.y
                            Utils.drawImage(
                                c,
                                icon,
                                px.toInt(),
                                py.toInt(),
                                icon.intrinsicWidth,
                                icon.intrinsicHeight
                            )
                        }

                        // draw stack values
                    } else {
                        val transformed = FloatArray(vals.size * 2)
                        var posY = 0f
                        var negY = -entry.negativeSum
                        run {
                            var k = 0
                            var idx = 0
                            while (k < transformed.size) {
                                val value = vals[idx]
                                var y: Float
                                if (value == 0.0f && (posY == 0.0f || negY == 0.0f)) {
                                    // Take care of the situation of a 0.0 value, which overlaps a non-zero bar
                                    y = value
                                } else if (value >= 0.0f) {
                                    posY += value
                                    y = posY
                                } else {
                                    y = negY
                                    negY -= value
                                }
                                transformed[k + 1] = y * phaseY
                                k += 2
                                idx++
                            }
                        }
                        trans.pointValuesToPixel(transformed)
                        var k = 0
                        while (k < transformed.size) {
                            val `val` = vals[k / 2]
                            val y = (transformed[k + 1] + offset)
                            if (!mViewPortHandler.isInBoundsRight(x)) break
                            if (!mViewPortHandler.isInBoundsY(y)
                                || !mViewPortHandler.isInBoundsLeft(x)
                            ) {
                                k += 2
                                continue
                            }
                            if (dataSet.isDrawValuesEnabled) {
                                drawValue(
                                    c,
                                    formatter.getBarStackedLabel(`val`, entry),
                                    x,
                                    y,
                                    color
                                )
                            }
                            if (entry.icon != null && dataSet.isDrawIconsEnabled) {
                                val icon = entry.icon
                                Utils.drawImage(
                                    c,
                                    icon,
                                    (x + iconsOffset.x).toInt(),
                                    (y + iconsOffset.y).toInt(),
                                    icon.intrinsicWidth,
                                    icon.intrinsicHeight
                                )
                            }
                            k += 2
                        }
                    }
                    bufferIndex =
                        if (vals == null) bufferIndex + 4 else bufferIndex + 4 * vals.size
                    index++
                }
            }
            MPPointF.recycleInstance(iconsOffset)
        }

    }
}