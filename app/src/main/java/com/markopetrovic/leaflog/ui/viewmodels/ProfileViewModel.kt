package com.markopetrovic.leaflog.ui.viewmodels

import com.markopetrovic.leaflog.data.repository.StorageRepository
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.markopetrovic.leaflog.data.models.ProfileDTO
import com.markopetrovic.leaflog.data.repository.ProfileRepository
import com.markopetrovic.leaflog.data.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val currentUserId: String,
    private val profileRepository: ProfileRepository,
    private val storageRepository: StorageRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _profile = MutableStateFlow<ProfileDTO?>(null)
    val profile: StateFlow<ProfileDTO?> = _profile.asStateFlow()

    private val _editableProfile = MutableStateFlow(ProfileDTO())
    val editableProfile: StateFlow<ProfileDTO> = _editableProfile.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveStatus = MutableStateFlow<String?>(null)
    val saveStatus: StateFlow<String?> = _saveStatus.asStateFlow()

    private val _totalLocationsCount = MutableStateFlow(0)
    val totalLocationsCount: StateFlow<Int> = _totalLocationsCount.asStateFlow()

    private val _totalPoints = MutableStateFlow(0)
    val totalPoints: StateFlow<Int> = _totalPoints.asStateFlow()

    init {
        loadProfile()
        loadStatistics()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            val fetchedProfile = profileRepository.getUserProfile(currentUserId)

            val initialProfile = fetchedProfile ?: ProfileDTO(uid = currentUserId, username = "New User")

            _profile.value = initialProfile
            _editableProfile.value = initialProfile.copy()
        }
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _totalLocationsCount.value = locationRepository.countUserLocations(currentUserId)

            val pointsLong = locationRepository.sumUserPoints(currentUserId)
            _totalPoints.value = pointsLong.toInt()

            _isLoading.value = false
        }
    }

    fun startEditingSession() {
        val profileDataToCopy = _profile.value ?: ProfileDTO(uid = currentUserId)

        _editableProfile.value = profileDataToCopy.copy()
        _selectedImageUri.value = null
        _saveStatus.value = null
    }

    fun setSelectedImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    fun updateFirstName(name: String) {
        _editableProfile.update { it.copy(firstName = name) }
    }

    fun updateLastName(name: String) {
        _editableProfile.update { it.copy(lastName = name) }
    }

    fun updateUsername(name: String) {
        _editableProfile.update { it.copy(username = name) }
    }

    fun saveProfile(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _saveStatus.value = null

            var profileToSave = _editableProfile.value.copy(uid = currentUserId)

            val imageUri = _selectedImageUri.value
            if (imageUri != null) {
                _saveStatus.value = "Uploading image..."

                val downloadUrl = storageRepository.uploadAvatar(currentUserId, imageUri, context)
                if (downloadUrl != null) {
                    profileToSave = profileToSave.copy(avatarUrl = downloadUrl)
                } else {
                    _saveStatus.value = "Image upload failed. Saving profile without new image."
                }
            }

            _saveStatus.value = "Saving profile data..."
            val success = profileRepository.updateProfile(profileToSave)

            if (success) {
                _profile.value = profileToSave
                _isEditing.value = false
                _selectedImageUri.value = null
                _saveStatus.value = "Profile saved successfully!"

                loadStatistics()
            } else {
                _saveStatus.value = "Failed to save profile. Please try again."
            }
            _isLoading.value = false
        }
    }
}

class ProfileViewModelFactory(
    private val currentUserId: String,
    private val profileRepository: ProfileRepository,
    private val storageRepository: StorageRepository,
    private val locationRepository: LocationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(
                currentUserId,
                profileRepository,
                storageRepository,
                locationRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
