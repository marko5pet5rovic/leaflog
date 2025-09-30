data class LeafLocationDTO(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val description: String?,
    val addedByUserId: String,
    val timestamp: Long,
    val averageRating: Double,
    val type: LocationType
) {
    companion object {
        fun fromDataMap(data: Map<String, Any>): LeafLocationDTO {
            return LeafLocationDTO(
                id = data["id"] as String,
                latitude = data["latitude"] as Double,
                longitude = data["longitude"] as Double,
                title = data["title"] as String,
                description = data["description"] as? String,
                addedByUserId = data["addedByUserId"] as String,
                timestamp = data["timestamp"] as Long,
                averageRating = data["averageRating"] as Double,
                type = LocationType.valueOf(data["type"] as String)
            )
        }
    }

    fun toDataMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "latitude" to latitude,
            "longitude" to longitude,
            "title" to title,
            "description" to description,
            "addedByUserId" to addedByUserId,
            "timestamp" to timestamp,
            "averageRating" to averageRating,
            "type" to type.name
        )
    }
}
