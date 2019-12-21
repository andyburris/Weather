package com.andb.apps.weather.ui.settings

import com.afollestad.materialdialogs.MaterialDialog
import de.Maxr1998.modernpreferences.Preference
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter

open class DialogPreference(key: String) : Preference(key) {

    private var contents: MaterialDialog.() -> Unit = {}

    fun contents(block: MaterialDialog.() -> Unit) {
        contents = block
    }

    override fun onClick(holder: PreferencesAdapter.ViewHolder) {
        super.onClick(holder)
        MaterialDialog(holder.itemView.context)
            .show(contents)
    }
}

// Preference DSL functions
inline fun PreferenceScreen.Builder.dialog(key: String, block: DialogPreference.() -> Unit) {
    addPreferenceItem(DialogPreference(key).apply(block))
}