package com.markopetrovic.leaflog.data.repository

import com.markopetrovic.leaflog.data.models.ProfileDTO
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {

    fun getLiveTopUsers(limit: Long): Flow<List<ProfileDTO>>

    suspend fun getTopUsers(limit: Long): List<ProfileDTO>

    suspend fun getUserProfile(uid: String): ProfileDTO?
    suspend fun updateProfile(profile: ProfileDTO): Boolean
}