package com.andb.apps.weather.ui.location

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andb.apps.weather.data.model.Location
import com.andb.apps.weather.data.repository.LocationRepo
import com.andb.apps.weather.util.debounce
import com.andb.apps.weather.util.mapAsync
import com.andb.apps.weather.util.newIoThread
import com.andb.apps.weather.util.toLiveData
import com.google.android.libraries.places.api.model.AutocompletePrediction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class LocationPickerViewModel(private val repo: LocationRepo) : ViewModel() {
    val savedLocations: LiveData<List<Location>> = repo.getAllLocations().toLiveData(viewModelScope)

    private val searchData = MutableLiveData<String>()
    val searchTerm: LiveData<String> = searchData
    val searchedLocations: LiveData<List<AutocompletePrediction>> =
        searchData
            .debounce(300)
            .mapAsync { term ->
                Log.d(
                    "locationPicker",
                    "term updated: $term"
                ); repo.getSuggestionsFromSearch(term)
            }

    fun updateSearch(term: String) {
        searchData.value = term
    }

    fun pickSearchedLocation(id: String) {
        newIoThread {
            val location = repo.getLocationByID(id) ?: return@newIoThread
            repo.saveLocation(location)
        }
    }

    fun removeLocation(location: Location) {
        newIoThread {
            repo.deleteLocation(location)
        }
    }

    fun selectSavedLocation(id: String) {
        repo.selectLocation(id)
    }
}