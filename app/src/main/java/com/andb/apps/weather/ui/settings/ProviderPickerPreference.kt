package com.andb.apps.weather.ui.settings

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.LinearLayout
import androidx.core.view.doOnNextLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.customview.customView
import com.andb.apps.dragdropper.dragDropWith
import com.andb.apps.weather.R
import com.andb.apps.weather.data.local.Prefs
import com.andb.apps.weather.data.model.Provider
import com.github.rongi.klaster.Klaster
import de.Maxr1998.modernpreferences.PreferenceScreen
import kotlinx.android.synthetic.main.settings_provider_item.view.*
import kotlinx.android.synthetic.main.settings_provider_layout.view.*

class ProviderPickerPreference(key: String) : DialogPreference(key) {

    private val colorUsed = Color.BLACK
    private val colorUnused = Color.GRAY

    init {

        contents {
            val view = layoutInflater.inflate(R.layout.settings_provider_layout, null)
            customView(view = view, noVerticalPadding = true)
            cornerRadius(literalDp = 8f)
            view.apply {
                providerPickerRecycler.layoutManager = LinearLayoutManager(context)

                val providers = Prefs.providers.toMutableList()
                lateinit var providerAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>

                val ith = providerPickerRecycler.dragDropWith {
                    onDropped { oldPos, newPos ->
                        providers.add(newPos, providers.removeAt(oldPos))
                        Prefs.providers = providers
                        providerAdapter.notifyDataSetChanged()
                    }
                }

                providerAdapter = providerAdapter(
                    providers = providers,
                    layoutInflater = layoutInflater,
                    minuteHeaderEnd = { providerPickerMinutesHeader.right }
                ) {
                    ith.startDrag(it)
                }

                providerPickerRecycler.adapter = providerAdapter

                providerPickerMinutesHeader.doOnNextLayout {
                    Log.d(
                        "providerPickerPref",
                        "providerPickerMinutesHeader.right = ${providerPickerMinutesHeader.right}"
                    )
                    providerAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun providerAdapter(
        providers: List<Provider>,
        layoutInflater: LayoutInflater,
        minuteHeaderEnd: () -> Int,
        onDrag: (RecyclerView.ViewHolder) -> Unit
    ) = Klaster.get()
        .itemCount { providers.size }
        .view(R.layout.settings_provider_item, layoutInflater)
        .bind { pos ->
            itemView.apply {
                val provider = providers[pos]
                providerItemName.text = provider.name
                providerItemMins.isVisible = provider.hasMinutely
                providerItemMins.imageTintList = ColorStateList.valueOf(
                    if (providers.filterIndexed { index, p -> index < pos && p.hasMinutely }
                            .isNotEmpty())
                        colorUnused
                    else
                        colorUsed
                )
                providerItemDays.text = provider.days.toString()
                providerItemDays.setTextColor(
                    if (providers.filterIndexed { index, p -> index < pos && p.days > provider.days }
                            .isNotEmpty())
                        colorUnused
                    else
                        colorUsed
                )

                providerItemMins.updateLayoutParams<LinearLayout.LayoutParams> {
                    Log.d(
                        "providerPickerPref",
                        "providerItemDays.x = ${providerItemDays.x}, minuteHeaderEnd = ${minuteHeaderEnd.invoke()}"
                    )
                    marginEnd = (providerItemDays.x - minuteHeaderEnd.invoke()).toInt()
                    Log.d("providerPickerPref", "marginEnd = $marginEnd")
                }

                providerItemDragHandle.setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        onDrag.invoke(this@bind)
                    }
                    false
                }

            }
        }
        .build()

    companion object {

    }
}

// Preference DSL functions
inline fun PreferenceScreen.Builder.providers(
    key: String,
    block: ProviderPickerPreference.() -> Unit
) {
    addPreferenceItem(ProviderPickerPreference(key).apply(block))
}