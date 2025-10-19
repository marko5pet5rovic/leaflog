package com.markopetrovic.leaflog.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.markopetrovic.leaflog.data.repository.LocationRepository
import com.markopetrovic.leaflog.data.repository.ProfileRepository
import com.markopetrovic.leaflog.data.models.LocationBase
import com.markopetrovic.leaflog.data.models.ProfileDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class RankingViewModel(
    private val locationRepository: LocationRepository,
    private val profileRepository: ProfileRepository,
    private val currentUserId: String?
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val locationRankings: StateFlow<List<LocationBase>> = locationRepository
        .getLiveTopLocations(10)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val topUsersFlow = profileRepository.getLiveTopUsers(10)

    private val _userRankings = MutableStateFlow<List<ProfileDTO>>(emptyList())
    val userRankings: StateFlow<List<ProfileDTO>> = _userRankings.asStateFlow()


    init {
        combineUserRankings()

        viewModelScope.launch {
            _userRankings.collect {
                if (_isLoading.value) {
                    _isLoading.value = false
                }
            }
        }
    }
    private fun combineUserRankings() {
        viewModelScope.launch {
            topUsersFlow.collect { fetchedUsers ->
                var mutableUsers = fetchedUsers.toMutableList()

                if (!currentUserId.isNullOrEmpty()) {

                    val currentPointsLong = locationRepository.sumUserPoints(currentUserId)
                    val currentPoints = currentPointsLong.toInt()

                    val currentUserIndex = mutableUsers.indexOfFirst { it.uid == currentUserId }

                    if (currentUserIndex != -1) {
                        val userProfile = mutableUsers[currentUserIndex]
                        mutableUsers[currentUserIndex] = userProfile.copy(totalPoints = currentPoints)
                    } else {
                        val currentUserProfile = profileRepository.getUserProfile(currentUserId)
                        if (currentUserProfile != null) {
                            mutableUsers.add(currentUserProfile.copy(totalPoints = currentPoints))
                        }
                    }
                    mutableUsers = mutableUsers
                        .sortedByDescending { it.totalPoints }
                        .take(10)
                        .toMutableList()
                }
                _userRankings.value = mutableUsers
            }
        }
    }
    class RankingViewModelFactory(
        private val locationRepository: LocationRepository,
        private val profileRepository: ProfileRepository,
        private val currentUserId: String?
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RankingViewModel::class.java)) {
                return RankingViewModel(
                    locationRepository = locationRepository,
                    profileRepository = profileRepository,
                    currentUserId = currentUserId
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}