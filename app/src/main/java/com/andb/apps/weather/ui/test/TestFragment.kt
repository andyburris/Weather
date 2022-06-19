package com.andb.apps.weather.ui.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.andb.apps.weather.R
import com.andb.apps.weather.ui.main.weatherView.MaterialWeatherView
import com.andb.apps.weather.ui.main.weatherView.WeatherView
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.test_layout.*

class TestFragment : DialogFragment() {

    val testWeatherView by lazy { MaterialWeatherView(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.test_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        testWeatherHolder.addView(testWeatherView)
        testWeatherView.apply {
            setDrawable(true)
            testWeatherView.setWeather(WeatherView.WEATHER_KIND_CLEAR, true)
        }

        testTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) {
                testWeatherView.setWeather((tab?.position ?: 0) + 1, testDaytimeSwitch.isChecked)
            }
        })

        testDaytimeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            testWeatherView.setWeather(testTabLayout.selectedTabPosition + 1, isChecked)
        }
    }
}
