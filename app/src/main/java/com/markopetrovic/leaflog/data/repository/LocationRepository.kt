package com.markopetrovic.leaflog.data.repository

import com.markopetrovic.leaflog.data.models.LocationBase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface LocationRepository {
    fun getAllLocations(): Flow<List<LocationBase>>
    fun getLiveTopLocations(limit: Long): Flow<List<LocationBase>>
    suspend fun addLocation(location: LocationBase): Boolean
    suspend fun getLocationById(id: String): LocationBase?
    suspend fun addPoints(locationId: String, userId: String, points: Int): Boolean
    suspend fun countUserLocations(userId: String): Int
    suspend fun sumUserPoints(userId: String): Long

    fun getLocationsWithinRadius(
        currentLat: Double,
        currentLon: Double,
        radiusMeters: Float
    ): Flow<List<LocationBase>>
}