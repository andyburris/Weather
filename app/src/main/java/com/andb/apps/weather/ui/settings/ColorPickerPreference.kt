package com.andb.apps.weather.ui.settings

import android.graphics.Color
import com.afollestad.materialdialogs.color.ColorPalette
import com.afollestad.materialdialogs.color.colorChooser
import com.andb.apps.weather.R
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter

class ColorPickerPreference(key: String) : DialogPreference(key) {

    private val color get() = getInt(Color.BLACK)

    var onSelect = {}

    init {
        contents {
            title(R.string.color_picker_title)
            colorChooser(
                colors = ColorPalette.Primary,
                subColors = ColorPalette.PrimarySub,
                allowCustomArgb = true,
                changeActionButtonsColor = true,
                initialSelection = color,
                selection = { md, color ->
                    md.dismiss()
                    commitInt(color)
                    requestRebind()
                    onSelect.invoke()
                })
            cornerRadius(8f)
        }
    }

    override fun bindViews(holder: PreferencesAdapter.ViewHolder) {
        super.bindViews(holder)
        (holder.widget as ColorView).apply {
            color = this@ColorPickerPreference.color
        }
    }

    override fun getWidgetLayoutResource(): Int = R.layout.settings_color_item
}

// Preference DSL functions
inline fun PreferenceScreen.Builder.colorPicker(
    key: String,
    block: ColorPickerPreference.() -> Unit
) {
    addPreferenceItem(ColorPickerPreference(key).apply(block))
}