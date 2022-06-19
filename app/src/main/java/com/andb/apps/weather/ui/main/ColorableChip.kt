package com.andb.apps.weather.ui.main

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.chip.Chip

class ColorableChip : Chip {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

/*    fun setChipBackgroundColor(color: Int) {
        this.chipDrawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }*/
}