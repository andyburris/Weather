package com.andb.apps.weather.ui.main

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.annotation.Nullable
import androidx.core.widget.NestedScrollView


class LockableNestedScrollView : NestedScrollView {
    // by default is scrollable
    private var scrollable = true

    constructor(context: Context) : super(context)
    constructor(context: Context, @Nullable attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, @Nullable attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return scrollable && super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return scrollable && super.onInterceptTouchEvent(ev)
    }

    fun setScrollingEnabled(enabled: Boolean) {
        Log.d("lockableNestedSV", "scrolling enabled = $enabled")
        scrollable = enabled
    }
}