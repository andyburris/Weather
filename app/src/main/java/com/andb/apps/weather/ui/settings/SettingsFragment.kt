package com.andb.apps.weather.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.andb.apps.dragdropper.dragDropWith
import com.andb.apps.weather.R
import de.Maxr1998.modernpreferences.Preference
import de.Maxr1998.modernpreferences.PreferencesAdapter
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
            dragDropWith {
                canDrag = { vh ->
                    preferencesAdapter.currentScreen[vh.adapterPosition] is ReorderablePreference
                }
                constrainDrag { vh ->
                    val draggedPref =
                        preferencesAdapter.currentScreen[vh.adapterPosition] as ReorderablePreference
                    val start = preferencesAdapter.preferences().lastIndexed { index, pref ->
                        val lastPref = preferencesAdapter.preferences().getOrNull(index - 1)
                        pref is ReorderablePreference
                                && pref.group == draggedPref.group
                                && (lastPref !is ReorderablePreference || lastPref.group != pref.group)
                    }.index
                    val end = preferencesAdapter.preferences().firstIndexed { index, pref ->
                        val nextPref = preferencesAdapter.preferences().getOrNull(index + 1)
                        pref is ReorderablePreference
                                && pref.group == draggedPref.group
                                && (nextPref !is ReorderablePreference || nextPref.group != pref.group)
                    }.index
                    return@constrainDrag start..end
                }
                onDropped { oldPos, newPos ->
                    (preferencesAdapter.currentScreen[oldPos] as ReorderablePreference).group.onDropped(
                        oldPos,
                        newPos
                    )
                }
            }
        }

        Log.d("settingsFragment", "adapter: ${settingsRecycler.adapter}")
    }

    fun backPossible() = this.isAdded && viewModel.adapter.isInSubScreen()
    fun goBack() = viewModel.adapter.goBack()
}

private fun PreferencesAdapter.preferences(): List<Preference> {
    return (0 until currentScreen.size()).map { currentScreen[it] }
}

private fun <T> List<T>.filterIndexed(predicate: (index: Int, T) -> Boolean): List<T> {
    val destination = mutableListOf<T>()
    for ((index, element) in this.withIndex()) if (predicate(index, element)) destination.add(
        element
    )
    return destination
}

public inline fun <T> Iterable<T>.firstIndexed(predicate: (index: Int, T) -> Boolean): IndexedValue<T> {
    for ((index, element) in this.withIndex()) if (predicate(index, element)) return IndexedValue(
        index,
        element
    )
    throw NoSuchElementException("Collection contains no element matching the predicate.")
}

public inline fun <T> Iterable<T>.lastIndexed(predicate: (index: Int, T) -> Boolean): IndexedValue<T> {
    for ((index, element) in this.withIndex().reversed()) if (predicate(
            index,
            element
        )
    ) return IndexedValue(index, element)
    throw NoSuchElementException("Collection contains no element matching the predicate.")
}