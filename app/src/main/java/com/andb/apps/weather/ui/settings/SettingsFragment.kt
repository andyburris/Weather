package com.andb.apps.weather.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.andb.apps.weather.R
import kotlinx.android.synthetic.main.settings_layout.*
import org.koin.android.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {
    private val viewModel: SettingsViewModel by viewModel()
    private val preferencesAdapter get() = viewModel.adapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_layout, container, false)
        //super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = preferencesAdapter
        }

        Log.d("settingsFragment", "adapter: ${settingsRecycler.adapter}")
    }

    fun backPossible() = this.isAdded && viewModel.adapter.isInSubScreen()
    fun goBack() = viewModel.adapter.goBack()
}