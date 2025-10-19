package com.markopetrovic.leaflog.ui.viewmodels.location.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.markopetrovic.leaflog.data.models.PlantingSpotDTO
import com.markopetrovic.leaflog.data.repository.LocationRepository
import com.markopetrovic.leaflog.ui.viewmodels.MapViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddNewPlantingSpotViewModel(
    private val locationRepository: LocationRepository,
    private val mapViewModel: MapViewModel
) : ViewModel() {

    private val initialLocation = mapViewModel.tempPlantLocation.value

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _isFenced = MutableStateFlow(false)
    val isFenced: StateFlow<Boolean> = _isFenced.asStateFlow()

    private val _soilType = MutableStateFlow("")
    val soilType: StateFlow<String> = _soilType.asStateFlow()


    fun updateName(newName: String) { _name.value = newName }
    fun updateDescription(newDescription: String) { _description.value = newDescription }
    fun toggleIsFenced() { _isFenced.value = !_isFenced.value }
    fun updateSoilType(newSoilType: String) { _soilType.value = newSoilType }


    fun savePlantingSpot() {
        val newPlantingSpotDTO = PlantingSpotDTO(
            id = "",
            name = _name.value,
            description = _description.value,
            latitude = initialLocation.latitude,
            longitude = initialLocation.longitude,
            isFenced = _isFenced.value,
            soilType = _soilType.value
        )

        viewModelScope.launch {
            locationRepository.addLocation(newPlantingSpotDTO)
            mapViewModel.setTempPlantLocation(LatLng(0.0, 0.0))
        }
    }
}

class AddNewPlantingSpotViewModelFactory(
    private val locationRepository: LocationRepository,
    private val mapViewModel: MapViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddNewPlantingSpotViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddNewPlantingSpotViewModel(locationRepository, mapViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}