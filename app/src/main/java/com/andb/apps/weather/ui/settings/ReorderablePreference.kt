package com.andb.apps.weather.ui.settings

import de.Maxr1998.modernpreferences.Preference
import de.Maxr1998.modernpreferences.PreferenceScreen

class ReorderableGroup(val builder: PreferenceScreen.Builder) {
    var onDropped: (oldPos: Int, newPos: Int) -> Unit = { _, _ -> }
}

inline fun PreferenceScreen.Builder.reorderableGroup(block: ReorderableGroup.() -> Unit) {
    val reorderableGroup = ReorderableGroup(this)
    block.invoke(reorderableGroup)
}

class ReorderablePreference(key: String, val group: ReorderableGroup) : Preference(key)

// Preference DSL functions
inline fun ReorderableGroup.reorderable(key: String, block: ReorderablePreference.() -> Unit) {
    builder.addPreferenceItem(ReorderablePreference(key, this).apply(block))
}