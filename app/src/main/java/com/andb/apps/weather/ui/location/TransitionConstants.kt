package com.andb.apps.weather.ui.location

import android.animation.TimeInterpolator
import android.view.animation.PathInterpolator

object TransitionConstants {
    val LINEAR_OUT_SLOW_IN: TimeInterpolator = PathInterpolator(0f, 0f, 0.2f, 1f)
    val FAST_OUT_SLOW_IN: TimeInterpolator = PathInterpolator(0.4f, 0f, 0.2f, 1f)
}
