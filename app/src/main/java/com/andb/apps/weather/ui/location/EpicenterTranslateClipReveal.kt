package com.andb.apps.weather.ui.location

import android.animation.*
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.transition.TransitionValues
import android.transition.Visibility
import android.util.AttributeSet
import android.util.Property
import android.view.View
import android.view.ViewGroup


/**
 * EpicenterTranslateClipReveal captures the clip bounds and translation values
 * before and after the scene change and animates between those and the
 * epicenter bounds during a visibility transition.
 */
class EpicenterTranslateClipReveal : Visibility {
    private val mInterpolatorX: TimeInterpolator?
    private val mInterpolatorY: TimeInterpolator?
    private val mInterpolatorZ: TimeInterpolator?

    constructor() {
        mInterpolatorX = null
        mInterpolatorY = null
        mInterpolatorZ = null
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        mInterpolatorX = TransitionConstants.LINEAR_OUT_SLOW_IN
        mInterpolatorY = TransitionConstants.FAST_OUT_SLOW_IN
        mInterpolatorZ = TransitionConstants.FAST_OUT_SLOW_IN
    }

    override fun captureStartValues(transitionValues: TransitionValues) {
        super.captureStartValues(transitionValues)
        captureValues(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        super.captureEndValues(transitionValues)
        captureValues(transitionValues)
    }

    private fun captureValues(values: TransitionValues) {
        val view = values.view
        if (view.visibility == View.GONE) {
            return
        }
        val bounds =
            Rect(0, 0, view.width, view.height)
        values.values[PROPNAME_BOUNDS] = bounds
        values.values[PROPNAME_TRANSLATE_X] = view.translationX
        values.values[PROPNAME_TRANSLATE_Y] = view.translationY
        values.values[PROPNAME_TRANSLATE_Z] = view.translationZ
        values.values[PROPNAME_Z] = view.z
        val clip = view.clipBounds
        values.values[PROPNAME_CLIP] = clip
    }

    override fun onAppear(
        sceneRoot: ViewGroup,
        view: View,
        startValues: TransitionValues,
        endValues: TransitionValues
    ): Animator {
        val endBounds = endValues.values[PROPNAME_BOUNDS] as Rect
        val startBounds = getEpicenterOrCenter(endBounds)
        val startX = startBounds.centerX() - endBounds.centerX().toFloat()
        val startY = startBounds.centerY() - endBounds.centerY().toFloat()
        val startZ = 0 - endValues.values[PROPNAME_Z] as Float

        // Translate the view to be centered on the epicenter.
        view.translationX = startX
        view.translationY = startY
        view.translationZ = startZ
        val endX = endValues.values[PROPNAME_TRANSLATE_X] as Float
        val endY = endValues.values[PROPNAME_TRANSLATE_Y] as Float
        val endZ = endValues.values[PROPNAME_TRANSLATE_Z] as Float
        val endClip = getBestRect(endValues)
        val startClip = getEpicenterOrCenter(endClip)

        // Prepare the view.
        view.clipBounds = startClip
        val startStateX =
            State(startClip.left, startClip.right, startX)
        val endStateX =
            State(endClip!!.left, endClip.right, endX)
        val startStateY =
            State(startClip.top, startClip.bottom, startY)
        val endStateY =
            State(endClip.top, endClip.bottom, endY)
        return createRectAnimator(
            view, startStateX, startStateY, startZ, endStateX, endStateY,
            endZ, endValues, mInterpolatorX, mInterpolatorY, mInterpolatorZ
        )
    }

    override fun onDisappear(
        sceneRoot: ViewGroup,
        view: View,
        startValues: TransitionValues,
        endValues: TransitionValues
    ): Animator {
        val startBounds = endValues.values[PROPNAME_BOUNDS] as Rect?
        val endBounds = getEpicenterOrCenter(startBounds)
        val endX = endBounds.centerX() - startBounds!!.centerX().toFloat()
        val endY = endBounds.centerY() - startBounds.centerY().toFloat()
        val endZ = 0 - startValues.values[PROPNAME_Z] as Float
        val startX = endValues.values[PROPNAME_TRANSLATE_X] as Float
        val startY = endValues.values[PROPNAME_TRANSLATE_Y] as Float
        val startZ = endValues.values[PROPNAME_TRANSLATE_Z] as Float
        val startClip = getBestRect(startValues)
        val endClip = getEpicenterOrCenter(startClip)

        // Prepare the view.
        view.clipBounds = startClip
        val startStateX = State(startClip!!.left, startClip.right, startX)
        val endStateX = State(endClip.left, endClip.right, endX)
        val startStateY = State(startClip.top, startClip.bottom, startY)
        val endStateY = State(endClip.top, endClip.bottom, endY)
        return createRectAnimator(
            view, startStateX, startStateY, startZ, endStateX, endStateY,
            endZ, endValues, mInterpolatorX, mInterpolatorY, mInterpolatorZ
        )
    }

    private fun getEpicenterOrCenter(bestRect: Rect?): Rect {
        val epicenter = epicenter
        if (epicenter != null) {
            return epicenter
        }
        val centerX = bestRect!!.centerX()
        val centerY = bestRect.centerY()
        return Rect(centerX, centerY, centerX, centerY)
    }

    private fun getBestRect(values: TransitionValues): Rect? {
        return values.values[PROPNAME_CLIP] as Rect?
            ?: return values.values[PROPNAME_BOUNDS] as Rect?
    }

    private class State {
        var lower = 0
        var upper = 0
        var trans = 0f

        constructor() {}
        constructor(lower: Int, upper: Int, trans: Float) {
            this.lower = lower
            this.upper = upper
            this.trans = trans
        }
    }

    private class StateEvaluator :
        TypeEvaluator<State> {
        private val mTemp = State()
        override fun evaluate(
            fraction: Float,
            startValue: State,
            endValue: State
        ): State {
            mTemp.upper =
                startValue.upper + ((endValue.upper - startValue.upper) * fraction).toInt()
            mTemp.lower =
                startValue.lower + ((endValue.lower - startValue.lower) * fraction).toInt()
            mTemp.trans =
                startValue.trans + ((endValue.trans - startValue.trans) * fraction).toInt()
            return mTemp
        }
    }

    private class StateProperty(targetDimension: Char) :
        Property<View, State>(
            State::class.java, "state_$targetDimension"
        ) {
        private val mTempRect = Rect()
        private val mTempState =
            State()
        private val mTargetDimension: Int = targetDimension.toInt()
        private fun getClipBounds(
            `object`: View,
            tempRect: Rect
        ): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                `object`.getClipBounds(tempRect)
            } else {
                val bounds = `object`.clipBounds
                if (bounds != null) {
                    tempRect.set(bounds)
                    true
                } else {
                    false
                }
            }
        }

        override fun get(`object`: View): State {
            val tempRect = mTempRect
            if (!getClipBounds(`object`, tempRect)) {
                tempRect.setEmpty()
            }
            val tempState = mTempState
            if (mTargetDimension == TARGET_X.toInt()) {
                tempState.trans = `object`.translationX
                tempState.lower = tempRect.left + tempState.trans.toInt()
                tempState.upper = tempRect.right + tempState.trans.toInt()
            } else {
                tempState.trans = `object`.translationY
                tempState.lower = tempRect.top + tempState.trans.toInt()
                tempState.upper = tempRect.bottom + tempState.trans.toInt()
            }
            return tempState
        }

        override fun set(
            `object`: View,
            value: State
        ) {
            val tempRect = mTempRect
            if (getClipBounds(`object`, tempRect)) {
                if (mTargetDimension == TARGET_X.toInt()) {
                    tempRect.left = value.lower - value.trans.toInt()
                    tempRect.right = value.upper - value.trans.toInt()
                } else {
                    tempRect.top = value.lower - value.trans.toInt()
                    tempRect.bottom = value.upper - value.trans.toInt()
                }
                `object`.clipBounds = tempRect
            }
            if (mTargetDimension == TARGET_X.toInt()) {
                `object`.translationX = value.trans
            } else {
                `object`.translationY = value.trans
            }
        }

        companion object {
            const val TARGET_X = 'x'
            const val TARGET_Y = 'y'
        }

    }

    companion object {
        private const val PROPNAME_CLIP = "android:epicenterReveal:clip"
        private const val PROPNAME_BOUNDS = "android:epicenterReveal:bounds"
        private const val PROPNAME_TRANSLATE_X = "android:epicenterReveal:translateX"
        private const val PROPNAME_TRANSLATE_Y = "android:epicenterReveal:translateY"
        private const val PROPNAME_TRANSLATE_Z = "android:epicenterReveal:translateZ"
        private const val PROPNAME_Z = "android:epicenterReveal:z"
        private fun createRectAnimator(
            view: View,
            startX: State,
            startY: State,
            startZ: Float,
            endX: State,
            endY: State,
            endZ: Float,
            endValues: TransitionValues,
            interpolatorX: TimeInterpolator?,
            interpolatorY: TimeInterpolator?,
            interpolatorZ: TimeInterpolator?
        ): Animator {
            val evaluator = StateEvaluator()
            val animZ = ObjectAnimator.ofFloat(view, View.TRANSLATION_Z, startZ, endZ)
            if (interpolatorZ != null) {
                animZ.interpolator = interpolatorZ
            }
            val propX = StateProperty(StateProperty.TARGET_X)
            val animX = ObjectAnimator.ofObject(view, propX, evaluator, startX, endX)
            if (interpolatorX != null) {
                animX.interpolator = interpolatorX
            }
            val propY = StateProperty(StateProperty.TARGET_Y)
            val animY = ObjectAnimator.ofObject(view, propY, evaluator, startY, endY)
            if (interpolatorY != null) {
                animY.interpolator = interpolatorY
            }
            val terminalClip = endValues.values[PROPNAME_CLIP] as Rect?
            val animatorListener: AnimatorListenerAdapter = object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.clipBounds = terminalClip
                }
            }
            val animSet = AnimatorSet()
            animSet.playTogether(animX, animY, animZ)
            animSet.addListener(animatorListener)
            animSet.duration = 200
            return animSet
        }
    }
}
