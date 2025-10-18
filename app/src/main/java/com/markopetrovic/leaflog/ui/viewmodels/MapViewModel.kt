package com.markopetrovic.leaflog.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markopetrovic.leaflog.data.repository.LocationRepository
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.markopetrovic.leaflog.data.models.LocationBase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.location.Location

private const val MAX_RADIUS_METERS = 50000f // 50km

enum class LocationFilter {
    ALL,
    PLANTS,
    MUSHROOMS,
    PLANTING_SPOTS
}

class MapViewModel(
    private val repository: LocationRepository
) : ViewModel() {

    private val defaultInitialLocation = LatLng(44.787197, 20.457273) //Beograd

    private val _currentGpsLocation = MutableStateFlow(defaultInitialLocation)
    val currentGpsLocation = _currentGpsLocation.asStateFlow()

    private val _cameraPosition = MutableStateFlow(
        CameraPosition.fromLatLngZoom(defaultInitialLocation, 8f)
    )
    val cameraPosition: StateFlow<CameraPosition> = _cameraPosition.asStateFlow()

    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet = _showBottomSheet.asStateFlow()

    private val _showFilterSheet = MutableStateFlow(false)
    val showFilterSheet: StateFlow<Boolean> = _showFilterSheet.asStateFlow()

    private val _tempPlantLocation = MutableStateFlow(LatLng(0.0, 0.0))
    val tempPlantLocation = _tempPlantLocation.asStateFlow()

    private val _activeFilter = MutableStateFlow(LocationFilter.ALL)
    val activeFilter: StateFlow<LocationFilter> = _activeFilter.asStateFlow()

    private val _radiusFilter = MutableStateFlow(MAX_RADIUS_METERS)
    val radiusFilter: StateFlow<Float> = _radiusFilter.asStateFlow()

    private val _locations = MutableStateFlow<List<LocationBase>>(emptyList())
    val locations: StateFlow<List<LocationBase>> = _locations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()


    init {
        loadLocations()
    }

    fun updateGpsLocation(latLng: LatLng) {
        _currentGpsLocation.value = latLng
        _cameraPosition.value = CameraPosition.fromLatLngZoom(latLng, 14f)
    }

    fun setTempPlantLocation(location: LatLng) {
        _tempPlantLocation.value = location
    }

    fun toggleBottomSheet(show: Boolean) {
        _showBottomSheet.value = show
    }

    fun setFilter(filter: LocationFilter) {
        _activeFilter.value = filter
    }

    fun setRadiusFilter(radius: Float) {
        _radiusFilter.value = radius
    }

    fun toggleFilterSheet(show: Boolean? = null) {
        _showFilterSheet.update { show ?: !it }
    }
    fun isLocationWithinRadius(location: LocationBase): Boolean {
        val currentRadius = _radiusFilter.value

        if (currentRadius >= MAX_RADIUS_METERS) {
            return true
        }

        if (_currentGpsLocation.value.latitude == 0.0 && _currentGpsLocation.value.longitude == 0.0) {
            return false
        }

        val loc1 = Location("").apply {
            latitude = _currentGpsLocation.value.latitude
            longitude = _currentGpsLocation.value.longitude
        }
        val loc2 = Location("").apply {
            latitude = location.latitude
            longitude = location.longitude
        }

        val distanceInMeters = loc1.distanceTo(loc2)
        return distanceInMeters <= currentRadius
    }

    private fun loadLocations() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllLocations().collectLatest { fetchedLocations ->
                _locations.value = fetchedLocations
                _isLoading.value = false
            }
        }
    }

    fun sendUiEvent(message: String) {
        viewModelScope.launch {
            _uiEvent.emit(message)
        }
    }
}
