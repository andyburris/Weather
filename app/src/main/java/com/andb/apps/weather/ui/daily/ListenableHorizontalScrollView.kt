package com.andb.apps.weather.ui.daily

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView
import kotlin.math.abs

class ObservableHorizontalScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : HorizontalScrollView(context, attrs, defStyle) {

    var onScrollChanged: ((x: Int, y: Int, oldX: Int, oldY: Int) -> Unit)? = null
    var onScrollEnd: (() -> Unit)? = null

    private var mIsScrolling = false
    private var mIsTouching = false
    private var mScrollingRunnable: Runnable? = null

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.action
        if (action == MotionEvent.ACTION_MOVE) {
            mIsTouching = true
            mIsScrolling = true
        } else if (action == MotionEvent.ACTION_UP) {
            if (mIsTouching && !mIsScrolling) {
                onScrollEnd?.invoke()
            }
            mIsTouching = false
        }
        return super.onTouchEvent(ev)
    }

    override fun onScrollChanged(x: Int, y: Int, oldX: Int, oldY: Int) {
        super.onScrollChanged(x, y, oldX, oldY)
        if (abs(oldX - x) > 0) {
            if (mScrollingRunnable != null) {
                removeCallbacks(mScrollingRunnable)
            }
            mScrollingRunnable = Runnable {
                if (mIsScrolling && !mIsTouching) {
                    onScrollEnd?.invoke()
                }
                mIsScrolling = false
                mScrollingRunnable = null
            }
            postDelayed(mScrollingRunnable, 200)
        }
        onScrollChanged?.invoke(x, y, oldX, oldY)
    }

}