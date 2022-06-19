package com.andb.apps.weather.ui.main

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import com.andb.apps.weather.R
import com.andb.apps.weather.data.local.Prefs
import com.andb.apps.weather.util.chipTextFrom
import com.andb.apps.weather.util.colorByNightModeRes
import com.andb.apps.weather.util.createChipStateList
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.chip_view.view.*

class ChipView : CardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private val colors = listOf(
        resources.getColor(R.color.chipDefaultDay),
        Prefs.colorTemperature,
        Prefs.colorRain,
        Prefs.colorUVIndex,
        Prefs.colorWind
    ).map { Pair(it, chipTextFrom(it)) }

    private val textIconColor = context.colorByNightModeRes(
        colorDayRes = R.color.chipDefaultDay,
        colorNightRes = R.color.chipDefaultNight
    )

    private val chipList by lazy {
        listOf(
            dailyChipSummary,
            dailyChipTemperature,
            dailyChipRain,
            dailyChipUV,
            dailyChipWind
        )
    }

    var onChipSelectedListener: (chip: Int, longClick: Boolean) -> Unit = { _, _ -> }

    init {
        inflate(context, R.layout.chip_view, this)

        chipList.forEachIndexed { index, chip ->
            chip.chipStrokeColor = ColorStateList.valueOf(colors[index].first)
            chip.chipBackgroundColor = createChipStateList(colors[index].first)
            chip.setTextColor(createChipStateList(colors[index].second, textIconColor))
            chip.chipIconTint = createChipStateList(colors[index].second, textIconColor)
        }

        setupClickListeners()
        selectChip(dailyChipSummary)
    }

    private fun setupClickListeners() {
        dailyChipSummary.setOnClickListener {
            selectChip(it as Chip)
            onChipSelectedListener.invoke(CHIP_DETAILS, false)
        }

        dailyChipTemperature.setOnClickListener {
            selectChip(it as Chip)
            onChipSelectedListener.invoke(CHIP_TEMP, false)
        }
        dailyChipTemperature.setOnLongClickListener {
            selectChip(it as Chip)
            onChipSelectedListener.invoke(CHIP_TEMP, true)
            return@setOnLongClickListener true
        }

        dailyChipRain.setOnClickListener {
            selectChip(it as Chip)
            onChipSelectedListener.invoke(CHIP_RAIN, false)
        }
        dailyChipRain.setOnLongClickListener {
            selectChip(it as Chip)
            onChipSelectedListener.invoke(CHIP_RAIN, true)
            true
        }

        dailyChipUV.setOnClickListener {
            selectChip(it as Chip)
            onChipSelectedListener.invoke(CHIP_UV, false)
        }

        dailyChipWind.setOnClickListener {
            selectChip(it as Chip)
            onChipSelectedListener.invoke(CHIP_WIND, false)
        }
    }

    private fun selectChip(chip: Chip) {
        chip.apply {
            isSelected = true
        }

        chipList.minus(chip).forEach {
            it.isSelected = false
        }
    }

    companion object {
        const val CHIP_DETAILS = 0
        const val CHIP_TEMP = 1
        const val CHIP_RAIN = 2
        const val CHIP_UV = 3
        const val CHIP_WIND = 4
    }

}