package com.andb.apps.weather.ui.location

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

open class PopupWindowFragment : Fragment() {

    internal val popupWindow by lazy {
        PopupWindowWithMaterialTransition(requireContext()).also {
            it.setOnDismissListener {
                Log.d("popupWindowFragment", "removing on window dismiss")
                parentFragmentManager.beginTransaction().remove(this).commit()
            }
        }
    }
    private lateinit var anchorView: View
    var elevation: Int = 0

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        popupWindow.contentView = view ?: throw Error("getView() is null")
        popupWindow.showWithAnchor(anchorView, Gravity.TOP or Gravity.LEFT)
        popupWindow.elevation = elevation.toFloat()
    }

    fun showWithAnchor(fragmentManager: FragmentManager, tag: String?, view: View) {
        anchorView = view
        fragmentManager.beginTransaction()
            .add(this, tag)
            .commit()
    }
}