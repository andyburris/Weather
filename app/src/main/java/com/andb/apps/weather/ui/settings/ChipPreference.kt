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

class ChipPreference(key: String) : Preference(key) {

    private var chipPreferenceItems = mutableListOf<ChipPreferenceItem>()
    var initialSelectValue = ""
    private var selectedId = View.NO_ID

    fun addChip(value: String, label: String, color: Int) {
        var id = Random.nextInt()
        while (chipPreferenceItems.any { it.id == id }) {
            id = Random.nextInt()
        }
        chipPreferenceItems.add(ChipPreferenceItem(id, value, label, color))
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
                    commitString(chipPreferenceItems.first { it.id == id }.value)
                    selectedId = id
                } else {
                    chipGroup.check(selectedId)
                }
            }

            val selected =
                chipPreferenceItems.indexOfFirst { it.value == getString(initialSelectValue) }
            selectedId = chipPreferenceItems[selected].id
            check(selectedId)
        }
    }

    override fun getWidgetLayoutResource(): Int = R.layout.settings_chips_item
}

private class ChipPreferenceItem(val id: Int, val value: String, val label: String, val color: Int)

// Preference DSL functions
inline fun PreferenceScreen.Builder.chips(key: String, block: ChipPreference.() -> Unit) {
    addPreferenceItem(ChipPreference(key).apply(block))
}

