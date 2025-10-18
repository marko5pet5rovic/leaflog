package com.markopetrovic.leaflog.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.markopetrovic.leaflog.data.models.PlantDTO
import com.markopetrovic.leaflog.data.repository.LocationRepository
import com.markopetrovic.leaflog.data.repository.StorageRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddNewPlantViewModel(
    private val locationRepository: LocationRepository,
    private val mapViewModel: MapViewModel,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val initialLocation = mapViewModel.tempPlantLocation.value

    private val _imagePath = MutableStateFlow<String?>(null)
    val imagePath: StateFlow<String?> = _imagePath.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _scientificName = MutableStateFlow("")
    val scientificName: StateFlow<String> = _scientificName.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _careTips = MutableStateFlow("")
    val careTips: StateFlow<String> = _careTips.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _navigationEvents = Channel<NavigationEvent>()
    val navigationEvents = _navigationEvents.receiveAsFlow()

    fun updateImagePath(path: String?) { _imagePath.value = path }
    fun updateName(newName: String) { _name.value = newName }
    fun updateScientificName(newScientificName: String) { _scientificName.value = newScientificName }
    fun updateDescription(newDescription: String) { _description.value = newDescription }
    fun updateCareTips(newCareTips: String) { _careTips.value = newCareTips }


    fun savePlant(context: Context) {
        if (_isSaving.value) return

        _isSaving.value = true
        val latLng = initialLocation

        viewModelScope.launch {
            var imageUrl: String? = null

            val localUriString = _imagePath.value
            if (localUriString != null) {
                try {
                    val localUri = Uri.parse(localUriString)
                    imageUrl = storageRepository.uploadPlantImage(
                        locationId = "${_name.value}_${System.currentTimeMillis()}",
                        uri = localUri,
                        context = context
                    )
                } catch (e: Exception) {
                    mapViewModel.sendUiEvent("Error uploading image: ${e.message}")
                }
            }

            val newPlantDTO = PlantDTO(
                id = "",
                name = _name.value,
                scientificName = _scientificName.value,
                description = _description.value,
                latitude = latLng.latitude,
                longitude = latLng.longitude,
                imageUrl = imageUrl,
                careTips = _careTips.value
            )

            val success = locationRepository.addLocation(newPlantDTO)

            if (success) {
                mapViewModel.sendUiEvent("Plant '${_name.value}' successfully saved!")
                mapViewModel.setTempPlantLocation(LatLng(0.0, 0.0))
                _navigationEvents.send(NavigationEvent.PopBackStack)
            } else {
                mapViewModel.sendUiEvent("Error saving location to database. (Check if you are logged in!)")
            }

            _isSaving.value = false
        }
    }
}

class AddNewPlantViewModelFactory(
    private val locationRepository: LocationRepository,
    private val mapViewModel: MapViewModel,
    private val storageRepository: StorageRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddNewPlantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddNewPlantViewModel(
                locationRepository,
                mapViewModel,
                storageRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}