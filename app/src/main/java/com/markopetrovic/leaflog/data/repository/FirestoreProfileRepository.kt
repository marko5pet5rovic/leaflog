package com.markopetrovic.leaflog.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.markopetrovic.leaflog.data.models.ProfileDTO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreProfileRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProfileRepository {

    private val collection = firestore.collection("users")

    override fun getLiveTopUsers(limit: Long): Flow<List<ProfileDTO>> = callbackFlow {
        val query = collection
            .orderBy("totalPoints", Query.Direction.DESCENDING)
            .limit(limit)

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                println("ERROR fetching live top users: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val users = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ProfileDTO::class.java)?.copy(uid = doc.id)
                }
                trySend(users)
            }
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun getUserProfile(uid: String): ProfileDTO? {
        return try {
            collection.document(uid).get().await().toObject(ProfileDTO::class.java)?.copy(uid = uid)
        } catch (e: Exception) {
            println("Error fetching user profile: ${e.message}")
            null
        }
    }

    override suspend fun updateProfile(profile: ProfileDTO): Boolean {
        return try {
            collection.document(profile.uid).set(profile).await()
            true
        } catch (e: Exception) {
            println("Error updating user profile: ${e.message}")
            false
        }
    }
}