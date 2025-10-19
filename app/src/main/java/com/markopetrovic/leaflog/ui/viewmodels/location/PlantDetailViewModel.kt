package com.markopetrovic.leaflog.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.markopetrovic.leaflog.data.models.LocationBase
import com.markopetrovic.leaflog.data.models.MushroomDTO
import com.markopetrovic.leaflog.data.models.PlantDTO
import com.markopetrovic.leaflog.data.models.PlantingSpotDTO
import com.markopetrovic.leaflog.data.models.ProfileDTO
import com.markopetrovic.leaflog.data.repository.LocationRepository
import com.markopetrovic.leaflog.data.repository.ProfileRepository
import com.markopetrovic.leaflog.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlantDetailViewModel(
    private val locationId: String,
    private val locationRepository: LocationRepository,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val currentUserId: String
        get() = when (val state = authRepository.authState.value) {
            is AuthState.Authenticated -> state.uid
            else -> ""
        }

    private val _location = MutableStateFlow<LocationBase?>(null)
    val location: StateFlow<LocationBase?> = _location.asStateFlow()

    private val _publisher = MutableStateFlow<ProfileDTO?>(null)
    val publisher: StateFlow<ProfileDTO?> = _publisher.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userAlreadyInteracted = MutableStateFlow(false)
    val userAlreadyInteracted: StateFlow<Boolean> = _userAlreadyInteracted.asStateFlow()

    init {
        loadLocationDetails()
    }

    private fun loadLocationDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val fetchedLocation = locationRepository.getLocationById(locationId)
                _location.value = fetchedLocation

                val userId = when (fetchedLocation) {
                    is PlantDTO -> fetchedLocation.userId
                    is MushroomDTO -> fetchedLocation.userId
                    is PlantingSpotDTO -> fetchedLocation.userId
                    else -> ""
                }

                if (userId.isNotBlank()) {
                    var userProfile = profileRepository.getUserProfile(userId)

                    if (userProfile != null) {
                        val totalPointsLong = locationRepository.sumUserPoints(userId)
                        userProfile = userProfile.copy(totalPoints = totalPointsLong.toInt())
                    }

                    _publisher.value = userProfile
                } else if (fetchedLocation != null) {
                    _publisher.value = ProfileDTO(username = "System Logger")
                }
            } catch (e: Exception) {
                println("ERROR fetching location details: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun givePoints() {
        viewModelScope.launch {
            val currentLocation = _location.value ?: return@launch
            val points = 1

            if (currentUserId.isBlank()) {
                println("ERROR: User not authenticated. Cannot give points.")
                return@launch
            }

            val success = locationRepository.addPoints(currentLocation.id, currentUserId, points)

            if (success) {
                val updatedLocation = when (currentLocation) {
                    is PlantDTO -> currentLocation.copy(points = currentLocation.points + points)
                    is MushroomDTO -> currentLocation.copy(points = currentLocation.points + points)
                    is PlantingSpotDTO -> currentLocation.copy(points = currentLocation.points + points)
                    else -> null
                }
                if (updatedLocation != null) {
                    _location.value = updatedLocation
                }
                _userAlreadyInteracted.value = true
            } else {
                _userAlreadyInteracted.value = true
            }
        }
    }
}

class PlantDetailViewModelFactory(
    private val locationId: String,
    private val locationRepository: LocationRepository,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlantDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlantDetailViewModel(
                locationId,
                locationRepository,
                profileRepository,
                authRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
