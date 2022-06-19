package com.andb.apps.weather.ui.location

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.andb.apps.swiper.swipeWith
import com.andb.apps.weather.R
import com.andb.apps.weather.data.model.Location
import com.andb.apps.weather.util.dp
import com.andb.apps.weather.util.observe
import com.andb.apps.weather.util.reset
import com.github.rongi.klaster.Klaster
import com.google.android.libraries.places.api.model.AutocompletePrediction
import kotlinx.android.synthetic.main.location_item.view.*
import kotlinx.android.synthetic.main.location_picker.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.math.max

@ExperimentalCoroutinesApi
@FlowPreview
class LocationPickerFragment : PopupWindowFragment() {

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
        return inflater.inflate(R.layout.location_picker, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationPickerRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = savedAdapter
            swipeWith {
                leftToRight {
                    endStep(TYPE_SAVED_LOCATION) {
                        icon(resources.getDrawable(R.drawable.ic_delete_black_24dp))
                        color(Color.RED)
                        iconColor(Color.WHITE)
                        action { viewModel.removeLocation(savedLocations[it.adapterPosition - 1]) }
                    }
                }
            }
        }

        locationPickerSearch.addTextChangedListener(onTextChanged = { text, start, count, after ->
            viewModel.updateSearch(text.toString())
        })

        locationPickerSearch.setOnFocusChangeListener { v, hasFocus ->


        }

        locationPickerClearIcon.setOnClickListener {
            locationPickerSearch.text.clear()
        }

        viewModel.savedLocations.observe(this@LocationPickerFragment) { savedLocations.reset(it); savedAdapter.notifyDataSetChanged() }
        viewModel.searchedLocations.observe(this@LocationPickerFragment) { searchLocations.reset(it); searchAdapter.notifyDataSetChanged() }
        viewModel.searchTerm.observe(this@LocationPickerFragment) { term ->
            TransitionManager.beginDelayedTransition(
                locationPickerRoot,
                ChangeBounds().apply { duration = 200 })

            if (term.isEmpty() && locationPickerRecycler.adapter != savedAdapter) {
                locationPickerRecycler.adapter = savedAdapter
            } else if (term.isNotEmpty() && locationPickerRecycler.adapter != searchAdapter) {
                locationPickerRecycler.adapter = searchAdapter
            }

            locationPickerSearchHolder.updateLayoutParams<ConstraintLayout.LayoutParams> {
                if (term.isEmpty()) updateMargins(16.dp, 16.dp, 16.dp) else updateMargins(0, 0, 0)
            }

            //locationPickerClearIcon.isVisible = term.isNotEmpty()
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        popupWindow.update(
            Resources.getSystem().displayMetrics.widthPixels - 64.dp,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun savedAdapter() = Klaster.get()
        .itemCount { savedLocations.size + 1 }
        .getItemViewType { pos ->
            if (pos == 0) TYPE_CURRENT_LOCATION else TYPE_SAVED_LOCATION
        }
        .view(R.layout.location_item, LayoutInflater.from(context))
        .bind { position ->
            when (position) {
                0 -> itemView.apply {
                    locationItemIcon.setImageResource(R.drawable.ic_my_location_black_24dp)
                    locationItemText.setText(R.string.current_location)
                    setOnClickListener {
                        viewModel.selectSavedLocation("")
                        activity?.supportFragmentManager?.popBackStack()
                    }
                }
                else -> itemView.apply {
                    locationItemIcon.setImageResource(R.drawable.ic_location_black_24dp)
                    val location = savedLocations[position - 1]
                    locationItemText.text = location.getText(context)
                    setOnClickListener {
                        viewModel.selectSavedLocation(location.id)
                        activity?.supportFragmentManager?.popBackStack()
                    }
                }
            }
        }.build()

    private fun searchAdapter() = Klaster.locationItem(
        list = searchLocations,
        emptyTextRes = R.string.no_search_results,
        onClick = {
            viewModel.pickSearchedLocation(it.placeId)
            locationPickerSearch.text.clear()
        },
        getText = {
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
    )

    private fun <T> Klaster.locationItem(
        list: MutableList<T>,
        emptyTextRes: Int,
        onClick: ((T) -> Unit)? = null,
        getText: (T) -> CharSequence
    ) = get()
        .itemCount { max(list.size, 1) }
        .view(R.layout.location_item, LayoutInflater.from(context))
        .bind { pos ->
            if (list.isEmpty()) {
                itemView.apply {
                    locationItemIcon.visibility = View.GONE
                    locationItemText.text = resources.getString(emptyTextRes)
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

    companion object {
        private const val TYPE_CURRENT_LOCATION = 387412
        private const val TYPE_SAVED_LOCATION = 342387
        private const val TYPE_SEARCH_LOCATION = 892344
    }
}