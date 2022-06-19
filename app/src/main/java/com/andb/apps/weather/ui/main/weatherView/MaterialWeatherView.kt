package com.andb.apps.weather.ui.main.weatherView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.IntDef
import androidx.annotation.Size
import androidx.core.graphics.ColorUtils
import com.andb.apps.weather.data.model.WeatherIcon
import com.andb.apps.weather.util.dpToPx
import com.andb.apps.weather.util.statusBarHeight
import kotlin.math.*

/**
 * Material Weather view.
 */

class MaterialWeatherView : View, WeatherView {

    private var intervalComputer: IntervalComputer? = null

    private var implementor: WeatherAnimationImplementor? = null
    private var rotators: Array<RotateController>? = null

    private var gravitySensorEnabled: Boolean = false
    private var sensorManager: SensorManager? = null
    private var gravitySensor: Sensor? = null

    @Size(2)
    internal lateinit var sizes: IntArray
    private var rotation2D: Float = 0.toFloat()
    private var rotation3D: Float = 0.toFloat()

    @WeatherView.WeatherKindRule
    private var weatherKind: Int = 0
    private var daytime: Boolean = false
    private val backgroundColor: Int
        get() = innerGetBackgroundColor(context, weatherKind, daytime)

    private var displayRate: Float = 0.toFloat()

    @StepRule
    private var step: Int = 0

    private var firstCardMarginTop: Int = 0
    private var scrollTransparentTriggerDistance: Int = 0

    private var lastScrollRate: Float = 0.toFloat()
    private var scrollRate: Float = 0.toFloat()

    private var drawable: Boolean = false

    private val gravityListener = object : SensorEventListener {

        override fun onSensorChanged(ev: SensorEvent) {
            // x : (+) fall to the left / (-) fall to the right.
            // y : (+) stand / (-) head stand.
            // z : (+) look down / (-) look up.
            // rotation2D : (+) anticlockwise / (-) clockwise.
            // rotation3D : (+) look down / (-) look up.
            if (gravitySensorEnabled) {
                val aX = ev.values[0]
                val aY = ev.values[1]
                val aZ = ev.values[2]
                val g2D = sqrt((aX * aX + aY * aY).toDouble())
                val g3D = sqrt((aX * aX + aY * aY + aZ * aZ).toDouble())
                val cos2D = max(min(1.0, aY / g2D), -1.0)
                val cos3D = max(min(1.0, g2D * (if (aY >= 0) 1 else -1) / g3D), -1.0)
                rotation2D = Math.toDegrees(acos(cos2D)).toFloat() * if (aX >= 0) 1 else -1
                rotation3D = Math.toDegrees(acos(cos3D)).toFloat() * if (aZ >= 0) 1 else -1

                if (60 < abs(rotation3D) && abs(rotation3D) < 120) {
                    rotation2D *= (abs(abs(rotation3D) - 90) / 30.0).toFloat()
                }
            } else {
                rotation2D = 0f
                rotation3D = 0f
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, i: Int) {
            // do nothing.
        }
    }

    @IntDef(STEP_DISPLAY, STEP_DISMISS)
    private annotation class StepRule

    /**
     * This class is used to implement different kinds of weather animations.
     */
    abstract class WeatherAnimationImplementor {

        abstract fun updateData(
            @Size(2) canvasSizes: IntArray, interval: Long,
            rotation2D: Float, rotation3D: Float
        )

        // return true if finish drawing.
        abstract fun draw(
            @Size(2) canvasSizes: IntArray, canvas: Canvas,
            displayRatio: Float, scrollRate: Float,
            rotation2D: Float, rotation3D: Float
        )
    }

    abstract class RotateController {

        abstract val rotation: Double

        abstract fun updateRotation(rotation: Double, interval: Double)
    }

    constructor(context: Context) : super(context) {
        this.initialize()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.initialize()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.initialize()
    }

    private fun initialize() {
        this.sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (sensorManager != null) {
            this.gravitySensorEnabled = true
            this.gravitySensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_GRAVITY)
        }

        this.step = STEP_DISPLAY
        setWeather(WeatherView.WEATHER_KIND_NULL, true)

        val metrics = resources.displayMetrics
        this.sizes = intArrayOf(metrics.widthPixels, metrics.heightPixels)

        firstCardMarginTop = max(
            firstCardMarginTop.toDouble(),
            resources.displayMetrics.heightPixels * 0.6
        ).toInt()
        scrollTransparentTriggerDistance = firstCardMarginTop
        -statusBarHeight(resources)
        -dpToPx(56)
        -dpToPx(16)


        this.lastScrollRate = 0f
        this.scrollRate = 0f

        this.drawable = false
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (measuredWidth != 0 && measuredHeight != 0) {
            sizes[0] = measuredWidth
            sizes[1] = measuredHeight
        }
        setWeatherImplementor()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (intervalComputer == null || rotators == null || implementor == null) {
            canvas.drawColor(backgroundColor)
            return
        }

        intervalComputer!!.invalidate()

        rotators!![0].updateRotation(rotation2D.toDouble(), intervalComputer!!.interval)
        rotators!![1].updateRotation(rotation3D.toDouble(), intervalComputer!!.interval)

        implementor!!.updateData(
            sizes, intervalComputer!!.interval.toLong(),
            rotators!![0].rotation.toFloat(), rotators!![1].rotation.toFloat()
        )

        displayRate =
            (displayRate + (if (step == STEP_DISPLAY) 1f else -1f) * intervalComputer!!.interval / SWITCH_ANIMATION_DURATION).toFloat()
        displayRate = Math.max(0f, displayRate)
        displayRate = Math.min(1f, displayRate)

        if (displayRate == 0f) {
            setWeatherImplementor()
        }

        canvas.drawColor(backgroundColor)
        if (implementor != null && rotators != null) {
            implementor!!.draw(
                sizes, canvas,
                displayRate, scrollRate,
                rotators!![0].rotation.toFloat(), rotators!![1].rotation.toFloat()
            )
        }
        if (lastScrollRate >= 1 && scrollRate >= 1) {
            lastScrollRate = scrollRate
            return
        }

        lastScrollRate = scrollRate

        postInvalidate()
    }

    private fun setWeatherImplementor() {
        step = STEP_DISPLAY
        implementor = WeatherImplementorFactory.getWeatherImplementor(weatherKind, daytime, sizes)
        rotators = arrayOf(
            DelayRotateController(rotation2D.toDouble()),
            DelayRotateController(rotation3D.toDouble())
        )
    }

    // interface.

    // weather view.

    fun setWeather(weatherIcon: WeatherIcon, daytime: Boolean) {
        Log.d("setWeather", "weatherIcon: $weatherIcon")
        when (weatherIcon) {
            WeatherIcon.CLEAR -> setWeather(WeatherView.WEATHER_KIND_CLEAR, daytime)
            WeatherIcon.RAIN -> setWeather(WeatherView.WEATHER_KIND_RAINY, daytime)
            WeatherIcon.THUNDERSTORM -> setWeather(WeatherView.WEATHER_KIND_THUNDERSTORM, daytime)
            WeatherIcon.SNOW -> setWeather(WeatherView.WEATHER_KIND_SNOW, daytime)
            WeatherIcon.SLEET -> setWeather(WeatherView.WEATHER_KIND_SLEET, daytime)
            WeatherIcon.HAIL -> setWeather(WeatherView.WEATHER_KIND_HAIL, daytime)
            WeatherIcon.WIND -> setWeather(WeatherView.WEATHER_KIND_WIND, daytime)
            WeatherIcon.FOG -> setWeather(WeatherView.WEATHER_KIND_FOG, daytime)
            WeatherIcon.CLOUDY -> setWeather(WeatherView.WEATHER_KIND_CLOUDY, daytime)
            WeatherIcon.PARTLY_CLOUDY -> setWeather(WeatherView.WEATHER_KIND_CLOUD, daytime)
            WeatherIcon.NONE -> setWeather(WeatherView.WEATHER_KIND_CLEAR, daytime)
        }
    }

    override fun setWeather(@WeatherView.WeatherKindRule weatherKind: Int, daytime: Boolean) {
        if (this.weatherKind == weatherKind && (isIgnoreDayNight(weatherKind) || this.daytime == daytime)) {
            return
        }

        this.weatherKind = weatherKind
        this.daytime = daytime

        if (drawable) {
            // Set step to dismiss. The implementor will execute exit animation and call weather
            // view to resetWidget it.
            step = STEP_DISMISS
        }
    }

    override fun onClick() {
        // do nothing.
    }

    override fun onScroll(scrollY: Int) {
        scrollRate = Math.min(1.0, 1.0 * scrollY / scrollTransparentTriggerDistance).toFloat()
        if (lastScrollRate >= 1 && scrollRate < 1) {
            postInvalidate()
        }
    }

    override fun getWeatherKind(): Int {
        return weatherKind
    }

    override fun getThemeColors(lightTheme: Boolean): IntArray {
        var color = backgroundColor
        if (!lightTheme) {
            color = getBrighterColor(color)
            return intArrayOf(
                color,
                color,
                ColorUtils.setAlphaComponent(color, (0.5 * 255).toInt())
            )
        } else {
            return intArrayOf(
                color,
                color,
                ColorUtils.setAlphaComponent(color, (0.5 * 255).toInt())
            )
        }
    }

    override fun getFirstCardMarginTop(): Int {
        return firstCardMarginTop
    }

    override fun setDrawable(drawable: Boolean) {
        if (this.drawable == drawable) {
            return
        }
        this.drawable = drawable

        if (drawable) {
            rotation3D = 0f
            rotation2D = rotation3D
            if (sensorManager != null) {
                sensorManager!!.registerListener(
                    gravityListener, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST
                )
            }

            setWeatherImplementor()

            if (intervalComputer == null) {
                intervalComputer = IntervalComputer()
            } else {
                intervalComputer!!.reset()
            }
        } else {
            // !drawable
            if (sensorManager != null) {
                sensorManager!!.unregisterListener(gravityListener, gravitySensor)
            }
        }
    }


    override fun setGravitySensorEnabled(enabled: Boolean) {
        this.gravitySensorEnabled = enabled
    }

    companion object {
        private const val STEP_DISPLAY = 1
        private const val STEP_DISMISS = -1

        private const val SWITCH_ANIMATION_DURATION = 150

        private fun getBrighterColor(color: Int): Int {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            hsv[1] = hsv[1] - 0.25f
            hsv[2] = hsv[2] + 0.25f
            return Color.HSVToColor(hsv)
        }

        private fun isIgnoreDayNight(@WeatherView.WeatherKindRule weatherKind: Int): Boolean {
            return (weatherKind == WeatherView.WEATHER_KIND_CLOUDY
                    || weatherKind == WeatherView.WEATHER_KIND_FOG
                    || weatherKind == WeatherView.WEATHER_KIND_HAZE
                    || weatherKind == WeatherView.WEATHER_KIND_THUNDERSTORM
                    || weatherKind == WeatherView.WEATHER_KIND_THUNDER
                    || weatherKind == WeatherView.WEATHER_KIND_WIND)
        }

        fun getThemeColors(
            context: Context,
            @WeatherView.WeatherKindRule weatherKind: Int, lightTheme: Boolean
        ): IntArray {
            var color = innerGetBackgroundColor(context, weatherKind, lightTheme)
            if (!lightTheme) {
                color = getBrighterColor(color)
                return intArrayOf(
                    color,
                    color,
                    ColorUtils.setAlphaComponent(color, (0.5 * 255).toInt())
                )
            } else {
                return intArrayOf(
                    color,
                    color,
                    ColorUtils.setAlphaComponent(color, (0.5 * 255).toInt())
                )
            }
        }

        private fun innerGetBackgroundColor(
            context: Context,
            @WeatherView.WeatherKindRule weatherKind: Int, daytime: Boolean
        ): Int {
            return WeatherImplementorFactory.getWeatherThemeColor(context, weatherKind, daytime)
        }

    }
}
