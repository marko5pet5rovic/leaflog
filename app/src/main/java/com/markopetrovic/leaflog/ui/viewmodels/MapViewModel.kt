package com.markopetrovic.leaflog.ui.viewmodels

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.markopetrovic.leaflog.data.models.LocationBase
import com.markopetrovic.leaflog.data.models.PlantingSpotDTO
import com.markopetrovic.leaflog.data.models.PlantDTO
import com.markopetrovic.leaflog.data.models.MushroomDTO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.markopetrovic.leaflog.di.AppContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import kotlin.time.Duration.Companion.seconds

private const val MAX_RADIUS_METERS = 5000f // 5km
private const val TAG = "MapViewModel"
enum class LocationFilter { ALL, PLANTS, MUSHROOMS, PLANTING_SPOTS }

class MapViewModel() : ViewModel() {

    private val repository = AppContainer.locationRepository
    private val defaultInitialLocation = LatLng(44.787197, 20.457273)

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
        startLocationCollection()
    }

    fun listenToGPSLocationChange(locProvider : FusedLocationProviderClient) {
        viewModelScope.launch {
            while (true) {
                try {
                    locProvider.lastLocation.addOnSuccessListener { location: Location? ->
                        location?.let {
                            updateGpsLocation(LatLng(it.latitude, it.longitude))
                        }
                    }
                } catch (e: SecurityException) { /* Handle security exception */ }
                delay(1.seconds)
            }
        }
    }
    fun hasDistanceChanged(
        latLng1: LatLng,
        latLng2: LatLng
    ): Boolean {

        val location1 = Location("point A").apply {
            latitude = latLng1.latitude
            longitude = latLng1.longitude
        }

        val location2 = Location("point B").apply {
            latitude = latLng2.latitude
            longitude = latLng2.longitude
        }

        val distanceInMeters = location1.distanceTo(location2)

        return distanceInMeters > 5.0f
    }

    fun updateGpsLocation(latLng: LatLng) {
        if (hasDistanceChanged(_currentGpsLocation.value, latLng)) {
            _currentGpsLocation.value = latLng
        }
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

    private fun startLocationCollection() {
        viewModelScope.launch {
            combine(_currentGpsLocation, _radiusFilter, _activeFilter) { location, radius, filter ->
                Triple(location, radius, filter)
            }.collectLatest { (location, radius, activeFilter) ->

                _isLoading.value = true

                repository.getLocationsWithinRadius(
                    currentLat = location.latitude,
                    currentLon = location.longitude,
                    radiusMeters = radius
                ).collectLatest { fetchedLocations ->

                    val finalLocations = fetchedLocations.filter { loc ->
                        when (activeFilter) {
                            LocationFilter.ALL -> true
                            LocationFilter.PLANTS -> loc is PlantDTO
                            LocationFilter.MUSHROOMS -> loc is MushroomDTO
                            LocationFilter.PLANTING_SPOTS -> loc is PlantingSpotDTO
                        }
                    }

                    _locations.value = finalLocations

                    _isLoading.value = false
                }
            }
        }
    }

    fun sendUiEvent(message: String) {
        viewModelScope.launch {
            _uiEvent.emit(message)
        }
    }
}
