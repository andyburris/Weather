package com.andb.apps.weather.ui.location

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andb.apps.weather.data.model.Location
import com.andb.apps.weather.data.repository.LocationRepo
import com.andb.apps.weather.util.asFlow
import com.andb.apps.weather.util.newIoThread
import com.andb.apps.weather.util.toLiveData
import com.google.android.libraries.places.api.model.AutocompletePrediction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map

@FlowPreview
@ExperimentalCoroutinesApi
class LocationPickerViewModel(private val repo: LocationRepo) : ViewModel() {
    val savedLocations: LiveData<List<Location>> = repo.getAllLocations().toLiveData(viewModelScope)

    val searchData = MutableLiveData<String>()
    val searchedLocations: LiveData<List<AutocompletePrediction>> =
        searchData.asFlow()
            .debounce(300)
            .map { repo.getSuggestionsFromSearch(it) }
            .toLiveData(viewModelScope)

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