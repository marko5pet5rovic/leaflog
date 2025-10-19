package com.markopetrovic.leaflog.ui.viewmodels.location.create

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.markopetrovic.leaflog.data.models.MushroomDTO
import com.markopetrovic.leaflog.data.repository.LocationRepository
import com.markopetrovic.leaflog.data.repository.StorageRepository
import com.markopetrovic.leaflog.ui.viewmodels.MapViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddNewMushroomViewModel(
    private val locationRepository: LocationRepository,
    private val storageRepository: StorageRepository,
    private val mapViewModel: MapViewModel
) : ViewModel() {

    private val initialLocation = mapViewModel.tempPlantLocation.value

    private val _imagePath = MutableStateFlow<String?>(null)
    val imagePath: StateFlow<String?> = _imagePath.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _isEdible = MutableStateFlow(false)
    val isEdible: StateFlow<Boolean> = _isEdible.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()


    fun updateImagePath(path: String?) { _imagePath.value = path }
    fun updateName(newName: String) { _name.value = newName }
    fun updateDescription(newDescription: String) { _description.value = newDescription }
    fun toggleIsEdible() { _isEdible.value = !_isEdible.value }


    fun saveMushroom(context: Context, onComplete: () -> Unit) {
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
                    mapViewModel.sendUiEvent("Error adding picture: ${e.message}")
                }
            }

            val newMushroomDTO = MushroomDTO(
                name = _name.value,
                description = _description.value,
                latitude = latLng.latitude,
                longitude = latLng.longitude,
                points = 0,
                imageUrl = imageUrl,
                isEdible = _isEdible.value,
                habitat = ""
            )

            val success = locationRepository.addLocation(newMushroomDTO)

            if (success) {
                mapViewModel.sendUiEvent("Mushroom '${_name.value}' added!")
                mapViewModel.setTempPlantLocation(LatLng(0.0, 0.0))
                onComplete()
            } else {
                mapViewModel.sendUiEvent("Error adding the new mushroom")
            }

            _isSaving.value = false
        }
    }
}

class AddNewMushroomViewModelFactory(
    private val locationRepository: LocationRepository,
    private val storageRepository: StorageRepository,
    private val mapViewModel: MapViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddNewMushroomViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddNewMushroomViewModel(locationRepository, storageRepository, mapViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}