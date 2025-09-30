import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Query.Direction
import com.google.firebase.firestore.Transaction
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.tasks.await
import java.lang.Exception


class RealFirebaseDatabase(
    public val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun collection(name: String): CollectionReference { 
      return firestore.collection(name) 
    }
    
    suspend fun getDocument(collection: String, documentId: String): Map<String, Any>? {
        try {
            val snapshot = collection(collection).document(documentId).get().await()
            return snapshot.data 
        } catch (e: Exception) {
            println("ERROR FETCHING $collection/$documentId: $e")
            return null
        }
    }

    suspend fun setDocument(collection: String, documentId: String, data: Map<String, Any?>) {
        collection(collection).document(documentId).set(data).await()
    }
    
    suspend fun getRankedUsers(limit: Int): List<Map<String, Any>> {
        val snapshot = collection("users")
            .orderBy("points", Direction.DESCENDING) 
            .limit(limit.toLong())
            .get().await()
        return snapshot.documents.mapNotNull { it.data } 
    }
    
    suspend fun incrementPoints(uid: String, pointsToAdd: Int): Unit {
        val userRef = collection("users").document(uid)
        
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentPoints = snapshot.getLong("points") ?: 0
            val newPoints = currentPoints + pointsToAdd
            transaction.update(userRef, "points", newPoints)
            null
        }.await()
    }
    
    suspend fun queryDocuments(collection: String, field: String, value: Any): List<Map<String, Any>> {
        val snapshot = collection(collection)
            .whereEqualTo(field, value)
            .get().await()
        return snapshot.documents.mapNotNull { it.data }
    }
    
    suspend fun performGeoQuery(lat: Double, lon: Double, radius: Double): List<Map<String, Any>> {
        return emptyList()
    }
    
    suspend fun deleteDocument(collection: String, documentId: String): Unit {
        collection(collection).document(documentId).delete().await()
    }

    fun generateNewId(collection: String): String {
        return collection(collection).document().id
    }
}
