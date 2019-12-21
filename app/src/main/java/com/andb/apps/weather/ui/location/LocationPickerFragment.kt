package com.andb.apps.weather.ui.location

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.andb.apps.swiper.swipeWith
import com.andb.apps.weather.R
import com.andb.apps.weather.data.model.Location
import com.andb.apps.weather.util.observe
import com.andb.apps.weather.util.reset
import com.github.rongi.klaster.Klaster
import com.google.android.libraries.places.api.model.AutocompletePrediction
import kotlinx.android.synthetic.main.location_item.view.*
import kotlinx.android.synthetic.main.location_picker_layout.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.math.max

@ExperimentalCoroutinesApi
@FlowPreview
class LocationPickerFragment : Fragment() {

    private val viewModel: LocationPickerViewModel by viewModel()

    private var savedLocations: MutableList<Location> = mutableListOf()
    private var searchLocations: MutableList<AutocompletePrediction> = mutableListOf()
    private val savedAdapter by lazy { savedAdapter() }
    private val searchAdapter by lazy { searchAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.location_picker_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationSelectorCurrent.locationItemText.text =
            resources.getString(R.string.current_location)
        locationSavedRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = savedAdapter
            swipeWith {
                right {
                    endStep {
                        icon = ContextCompat.getDrawable(context, R.drawable.ic_delete_black_24dp)
                        color = Color.RED
                        iconColor = Color.WHITE
                        action = { viewModel.removeLocation(savedLocations[it]) }
                    }
                }
            }
        }
        locationSearchRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchAdapter
        }
        locationSelectorSearchInputLayout.editText!!.addTextChangedListener(onTextChanged = { text, start, count, after ->
            viewModel.updateSearch(text.toString())
        })
        locationSelectorCurrent.setOnClickListener {
            viewModel.selectSavedLocation("")
            activity?.supportFragmentManager?.popBackStack()
        }

        viewModel.savedLocations.observe(viewLifecycleOwner) { savedLocations.reset(it); savedAdapter.notifyDataSetChanged() }
        viewModel.searchedLocations.observe(viewLifecycleOwner) { searchLocations.reset(it); searchAdapter.notifyDataSetChanged() }
    }

    fun savedAdapter() = Klaster.locationItem(
        savedLocations,
        onClick = { viewModel.selectSavedLocation(it.id); activity?.supportFragmentManager?.popBackStack() }) {
        String.format(
            resources.getString(R.string.location_placeholder),
            it.name,
            it.region
        )
    }

    fun searchAdapter() = Klaster.locationItem(
        searchLocations,
        onClick = { viewModel.pickSearchedLocation(it.placeId) }) {
        val fullText = it.getFullText(null)
        val primaryText = it.getPrimaryText(null)
        val boldStart = fullText.indexOf(primaryText.toString())
        fullText.setSpan(
            StyleSpan(Typeface.BOLD),
            boldStart,
            boldStart + primaryText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return@locationItem fullText
    }

    private fun <T> Klaster.locationItem(
        list: MutableList<T>,
        onClick: ((T) -> Unit)? = null,
        getText: (T) -> CharSequence
    ) = get()
        .itemCount { max(list.size, 1) }
        .view(R.layout.location_item, layoutInflater)
        .bind { pos ->
            if (list.isEmpty()) {
                itemView.apply {
                    locationItemIcon.visibility = View.GONE
                    locationItemText.text = resources.getString(R.string.no_saved_locations)
                }
            } else {
                val location = list[pos]
                itemView.apply {
                    locationItemIcon.visibility = View.VISIBLE
                    locationItemIcon.setImageResource(R.drawable.ic_location_black_24dp)
                    locationItemText.typeface = Typeface.DEFAULT
                    locationItemText.text = getText.invoke(location)
                    setOnClickListener { onClick?.invoke(location) }
                }
            }

        }
        .build()
}