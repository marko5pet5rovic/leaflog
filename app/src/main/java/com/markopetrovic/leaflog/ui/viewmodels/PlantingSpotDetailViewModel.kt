package com.markopetrovic.leaflog.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.markopetrovic.leaflog.data.models.PlantingSpotDTO
import com.markopetrovic.leaflog.data.models.ProfileDTO
import com.markopetrovic.leaflog.data.repository.LocationRepository
import com.markopetrovic.leaflog.data.repository.AuthRepository
import com.markopetrovic.leaflog.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlantingSpotDetailViewModel(
    initialLocation: PlantingSpotDTO,
    private val locationRepository: LocationRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val currentUserId: String
        get() = when (val state = authRepository.authState.value) {
            is AuthState.Authenticated -> state.uid
            else -> ""
        }

    private val _location = MutableStateFlow(initialLocation)
    val location: StateFlow<PlantingSpotDTO> = _location.asStateFlow()

    private val _publisher = MutableStateFlow<ProfileDTO?>(null)
    val publisher: StateFlow<ProfileDTO?> = _publisher.asStateFlow()

    private val _userAlreadyInteracted = MutableStateFlow(false)
    val userAlreadyInteracted: StateFlow<Boolean> = _userAlreadyInteracted.asStateFlow()

    init {
        checkUserInteraction()
        fetchPublisherDetails()
    }

    private fun fetchPublisherDetails() {
        viewModelScope.launch {
            try {
                val userId = _location.value.userId
                if (userId.isNotBlank()) {
                    val userProfile = profileRepository.getUserProfile(userId)
                    _publisher.value = userProfile
                } else {
                    _publisher.value = ProfileDTO(username = "System Logger")
                }
            } catch (e: Exception) {
                _publisher.value = ProfileDTO(username = "Unknown User (Error)")
            }
        }
    }

    private fun checkUserInteraction() {
        viewModelScope.launch {
            if (currentUserId.isNotBlank()) {
                _userAlreadyInteracted.value = locationRepository.hasUserInteracted(
                    locationId = _location.value.id,
                    userId = currentUserId
                )
            }
        }
    }

    fun givePoints() {
        viewModelScope.launch {
            val currentLocation = _location.value
            val points = 1

            if (_userAlreadyInteracted.value) return@launch
            if (currentUserId.isBlank()) {
                println("ERROR: User not authenticated. Cannot give points.")
                return@launch
            }

            val success = locationRepository.addPoints(currentLocation.id, currentUserId, points)

            if (success) {
                val updatedLocation = currentLocation.copy(points = currentLocation.points + points)
                _location.value = updatedLocation
                _userAlreadyInteracted.value = true
            } else {
                _userAlreadyInteracted.value = true
            }
        }
    }
}

class PlantingSpotDetailViewModelFactory(
    private val initialLocation: PlantingSpotDTO,
    private val locationRepository: LocationRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlantingSpotDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlantingSpotDetailViewModel(
                initialLocation,
                locationRepository,
                authRepository,
                profileRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}