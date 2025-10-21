package com.markopetrovic.leaflog.data.repository

import InteractionDTO
import android.location.Location
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import com.google.firebase.auth.FirebaseAuth
import com.markopetrovic.leaflog.data.models.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreLocationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth
) : LocationRepository {

    private val collection = firestore.collection("locations")
    private val usersCollection = firestore.collection("users")
    private fun getInteractionsCollection(locationId: String) =
        collection.document(locationId).collection("interactions")

    private fun mapDocumentToLocationBase(doc: DocumentSnapshot): LocationBase? {
        val typeKey = if (doc.contains("typeString")) "typeString" else "type"
        val typeValue = doc.getString(typeKey) ?: return null

        val locationType = LocationType.entries.firstOrNull { it.typeName == typeValue }

        return when (locationType) {
            LocationType.PLANT -> doc.toObject(PlantDTO::class.java)?.copy(id = doc.id)
            LocationType.MUSHROOM -> doc.toObject(MushroomDTO::class.java)?.copy(id = doc.id)
            LocationType.PLANTING_SPOT -> doc.toObject(PlantingSpotDTO::class.java)?.copy(id = doc.id)
            LocationType.GENERIC_LOCATION, null -> null
        }
    }

    override fun getLiveTopLocations(limit: Long): Flow<List<LocationBase>> = callbackFlow {
        val query = collection
            .orderBy("points", Query.Direction.DESCENDING)
            .limit(limit)

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (snapshot != null) {
                val locations = snapshot.documents.mapNotNull { doc -> mapDocumentToLocationBase(doc) }
                trySend(locations)
            }
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun addLocation(location: LocationBase): Boolean {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId.isNullOrEmpty()) {
            println("ERROR: User not logged in. Cannot save location.")
            return false
        }
        val locationWithUserId = when (location) {
            is PlantDTO -> location.copy(userId = currentUserId)
            is MushroomDTO -> location.copy(userId = currentUserId)
            is PlantingSpotDTO -> location.copy(userId = currentUserId)
            else -> location
        }
        return try {
            collection.add(locationWithUserId).await()
            true
        } catch (e: Exception) {
            println("Error adding location: ${e.message}")
            false
        }
    }

    override suspend fun getLocationById(id: String): LocationBase? {
        val doc = collection.document(id).get().await();
        return mapDocumentToLocationBase(doc)
    }

    override suspend fun addPoints(locationId: String, userId: String, points: Int): Boolean {
        val interactionDocRef = getInteractionsCollection(locationId).document(userId)
        val locationDocRef = collection.document(locationId)
        val userProfileRef = usersCollection.document(userId)

        return try {
            firestore.runTransaction { transaction ->
                if (transaction.get(interactionDocRef).exists()) {
                    throw IllegalStateException("User already interacted (points).")
                }

                transaction.update(locationDocRef, "points", FieldValue.increment(points.toLong()))
                transaction.update(userProfileRef, "totalPoints", FieldValue.increment(points.toLong()))

                val interaction = InteractionDTO(
                    userId = userId,
                    locationId = locationId,
                    pointsGiven = points
                )
                transaction.set(interactionDocRef, interaction)
                true
            }.await()
        } catch (e: IllegalStateException) {
            println("User already interacted: ${e.message}")
            false
        } catch (e: Exception) {
            println("ERROR adding points: ${e.message}")
            false
        }
    }

    override suspend fun countUserLocations(userId: String): Int {
        val snapshot = collection.whereEqualTo("userId", userId).get().await()
        return snapshot.size()
    }

    override suspend fun sumUserPoints(userId: String): Long {
        val snapshot = collection.whereEqualTo("userId", userId).get().await()
        return snapshot.documents.sumOf { doc -> (doc.get("points") as? Number)?.toLong() ?: 0L }
    }

    override fun getLocationsWithinRadius(
        currentLat: Double,
        currentLon: Double,
        radiusMeters: Float
    ): Flow<List<LocationBase>> = callbackFlow {
        val userLocation = Location("User").apply {
            latitude = currentLat
            longitude = currentLon
        }

        val query = collection
            .whereGreaterThanOrEqualTo("latitude", currentLat - radiusMeters)
            .whereLessThanOrEqualTo("latitude", currentLat + radiusMeters)
            .whereGreaterThanOrEqualTo("longitude", currentLon - radiusMeters)
            .whereLessThanOrEqualTo("longitude", currentLon + radiusMeters)

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }

            if (snapshot != null) {
                val allLocations = snapshot.documents.mapNotNull { doc -> mapDocumentToLocationBase(doc) }

                val filteredLocations = allLocations.filter { locationBase ->

                    val locationPoint = Location("LocationPoint").apply {
                        latitude = locationBase.latitude
                        longitude = locationBase.longitude
                    }

                    val distance = userLocation.distanceTo(locationPoint)

                    distance <= radiusMeters
                }

                trySend(filteredLocations)
            }
        }
        awaitClose { subscription.remove() }
    }
}