package com.andb.apps.weather.ui.settings

import android.util.Log
import android.view.View
import com.andb.apps.weather.R
import com.andb.apps.weather.util.chipTextFrom
import com.andb.apps.weather.util.colorByNightModeRes
import com.andb.apps.weather.util.createChipStateList
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import de.Maxr1998.modernpreferences.Preference
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter
import kotlin.random.Random

class ChipPreference<T>(key: String, val defaultValue: T? = null) : Preference(key) {

    /** Function to save value if it is not one of default supported types (String, Int, or Boolean) */
    var customSave: ((value: T) -> Unit)? = null
    /** Function to load value if it is not one of default supported types (String, Int, or Boolean) */
    var customGet: ((key: String, defaultValue: T?) -> T)? = null

    private var chipPreferenceItems = mutableListOf<ChipPreferenceItem<T>>()
    private var selectedId = View.NO_ID

    fun addChip(value: T, label: String, color: Int) {
        chipPreferenceItems.add(ChipPreferenceItem(chipID(), value, label, color))
    }

    private fun chipID(): Int {
        var id = Random.nextInt()
        while (chipPreferenceItems.any { it.id == id }) {
            id = Random.nextInt()
        }
        return id
    }

    override fun bindViews(holder: PreferencesAdapter.ViewHolder) {
        super.bindViews(holder)
        (holder.widget as ChipGroup).apply {
            this.removeAllViews()
            chipPreferenceItems.forEach { chipPref ->
                this.addView(Chip(context).also {
                    it.id = chipPref.id
                    it.isCheckable = true
                    it.chipBackgroundColor =
                        createChipStateList(chipPref.color)
                    it.text = chipPref.label
                    it.setTextColor(
                        context.colorByNightModeRes(
                            R.color.chipDefaultDay,
                            R.color.chipDefaultNight
                        )
                    )
                    it.setOnCheckedChangeListener { compoundButton, checked ->
                        it.setTextColor(
                            if (checked) chipTextFrom(chipPref.color) else context.colorByNightModeRes(
                                R.color.chipDefaultDay,
                                R.color.chipDefaultNight
                            )
                        )
                    }
                })
            }
            isSingleSelection = true
            setOnCheckedChangeListener { chipGroup, id ->
                if (id != View.NO_ID) { //deselection
                    Log.d(
                        "chipPrefCheckChange",
                        "id: $id, chipIds: ${chipPreferenceItems.map { it.id }}"
                    )
                    val chipPreferenceItem = chipPreferenceItems.first { it.id == id }
                    when (chipPreferenceItem.value) {
                        is String -> commitString(chipPreferenceItem.value)
                        is Int -> commitInt(chipPreferenceItem.value)
                        is Boolean -> commitBoolean(chipPreferenceItem.value)
                        else -> customSave?.invoke(chipPreferenceItem.value)
                    }
                    selectedId = id
                } else {
                    chipGroup.check(selectedId)
                }
            }

            val initialSelect = when (defaultValue) {
                is String -> getString(defaultValue)
                is Int -> getInt(defaultValue)
                is Boolean -> getBoolean(defaultValue)
                else -> customGet?.invoke(key, defaultValue)
            }
            Log.d("chipPreference", "initialSelect: $initialSelect")
            val selected = chipPreferenceItems.indexOfFirst {
                it.value == initialSelect
            }.coerceAtLeast(0)
            selectedId = chipPreferenceItems[selected].id
            check(selectedId)
        }
    }

    override fun getWidgetLayoutResource(): Int = R.layout.settings_chips_item
}

private open class ChipPreferenceItem<T>(
    val id: Int,
    val value: T,
    val label: String,
    val color: Int
)

// Preference DSL functions
inline fun <T> PreferenceScreen.Builder.chips(
    key: String,
    defaultValue: T? = null,
    block: ChipPreference<T>.() -> Unit
) {
    addPreferenceItem(ChipPreference<T>(key, defaultValue).apply(block))
}

