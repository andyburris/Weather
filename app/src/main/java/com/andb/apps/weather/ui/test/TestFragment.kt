package com.andb.apps.weather.ui.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.andb.apps.weather.R
import com.andb.apps.weather.data.local.Prefs
import kotlinx.android.synthetic.main.test_layout.*

class TestFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.test_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hourPicker.is24Hour = Prefs.time24HrFormat
    }
}
