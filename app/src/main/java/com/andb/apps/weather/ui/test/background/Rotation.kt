package com.andb.apps.weather.ui.test.background

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.OrientationEventListener
import androidx.compose.runtime.*
import com.afollestad.materialdialogs.utils.MDUtil.isLandscape
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

internal data class RotationState(val rotation2D: Float, val rotation3D: Float)

@Composable
internal fun Context.getRotationState(): State<RotationState> {
    val (forceX, forceY, forceZ) = remember { getGravity() }.value
    val orientation = remember { getOrientation() }.value
    return derivedStateOf {
        calculateRotation(forceX, forceY, forceZ, orientation)
    }
}

private fun Context.getGravity(): State<Triple<Float, Float, Float>> {
    val rotationState = mutableStateOf(Triple(0f, 0f, 0f))
    val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val gravitySensor: Sensor = sensorManager
        .getDefaultSensor(Sensor.TYPE_GRAVITY)
    val gravityListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val (forceX, forceY, forceZ) = event.values
            rotationState.value = Triple(forceX, forceY, forceZ)
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }
    sensorManager.registerListener(
        gravityListener,
        gravitySensor,
        SensorManager.SENSOR_DELAY_FASTEST
    )
    return rotationState
}

enum class DeviceOrientation { Top, Bottom, Left, Right }

private fun Context.getOrientation(): State<DeviceOrientation> {
    val orientationState = mutableStateOf(DeviceOrientation.Top)
    val orientationListener = object : OrientationEventListener(this) {
        override fun onOrientationChanged(orientation: Int) {
            orientationState.value = when {
                this@getOrientation.isLandscape() -> when (orientation) {
                    in 1 until 180 -> DeviceOrientation.Right
                    else -> DeviceOrientation.Left
                }
                else -> when (orientation) {
                    in 90 until 270 -> DeviceOrientation.Bottom
                    else -> DeviceOrientation.Top
                }
            }
        }
    }
    orientationListener.enable()
    return orientationState
}


private fun calculateRotation(
    forceX: Float,
    forceY: Float,
    forceZ: Float,
    orientation: DeviceOrientation
): RotationState {
    val magnitude2D = sqrt(forceX.pow(2) + forceY.pow(2))
    val rotation2D: Float = when (magnitude2D) {
        0f -> 0f
        else -> acos(-forceX / magnitude2D).let {
            when (orientation) {
                DeviceOrientation.Top -> it
                DeviceOrientation.Bottom -> if (it > 0) it - 180.deg.toRadians()
                    .toFloat() else it + 180.deg.toRadians().toFloat()
                DeviceOrientation.Left -> it - 90.deg.toRadians().toFloat()
                DeviceOrientation.Right -> it + 90.deg.toRadians().toFloat()
            }
        }
    }
    val magnitude3D = sqrt(magnitude2D.pow(2) + forceZ.pow(2))
    val rotation3D = when (magnitude3D) {
        0f -> 0f
        else -> acos(forceZ / magnitude3D)
    }

    return RotationState(rotation2D, rotation3D)
}

typealias Degrees = Number
typealias Radians = Number

private val Number.deg: Degrees get() = this
private val Number.rad: Radians get() = this

fun Degrees.toRadians(): Double = Math.toRadians(this.toDouble())
fun Radians.toDegrees(): Double = Math.toDegrees(this.toDouble())