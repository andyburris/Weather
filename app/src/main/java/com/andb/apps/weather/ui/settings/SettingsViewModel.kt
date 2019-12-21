package com.andb.apps.weather.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import de.Maxr1998.modernpreferences.PreferencesAdapter

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    val adapter = PreferencesAdapter(SettingsLayout.create(getApplication()))
}