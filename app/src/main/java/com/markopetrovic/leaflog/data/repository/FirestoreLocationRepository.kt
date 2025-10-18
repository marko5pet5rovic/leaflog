package com.markopetrovic.leaflog.data.repository

import InteractionDTO
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

    override fun getAllLocations(): Flow<List<LocationBase>> = callbackFlow {
        val subscription = collection.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            if (snapshot != null) {
                val locations = snapshot.documents.mapNotNull { doc -> mapDocumentToLocationBase(doc) }
                trySend(locations)
            }
        }
        awaitClose { subscription.remove() }
    }

    override fun getLiveTopLocations(limit: Long): Flow<List<LocationBase>> = callbackFlow {
        val query = collection
            .orderBy("points", Query.Direction.DESCENDING)
            .limit(limit)

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                println("ERROR fetching live top locations: ${error.message}")
                return@addSnapshotListener
            }
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
        return try { val doc = collection.document(id).get().await(); mapDocumentToLocationBase(doc) } catch (e: Exception) { println("Error fetching location by ID: ${e.message}"); null }
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
        return try {
            val snapshot = collection.whereEqualTo("userId", userId).get().await()
            snapshot.size()
        } catch (e: Exception) {
            println("Error counting user locations: ${e.message}")
            0
        }
    }

    override suspend fun sumUserPoints(userId: String): Long {
        return try {
            val snapshot = collection.whereEqualTo("userId", userId).get().await()
            snapshot.documents.sumOf { doc -> (doc.get("points") as? Number)?.toLong() ?: 0L }
        } catch (e: Exception) {
            println("ERROR: Manual point summation failed: ${e.message}")
            0L
        }
    }
}