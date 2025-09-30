data class LeafRatingDTO(
    val locationId: String,
    val userId: String,
    val score: Int
) {
    companion object {
        fun fromDataMap(data: Map<String, Any>): LeafRatingDTO {
            return LeafRatingDTO(
                locationId = data["locationId"] as String,
                userId = data["userId"] as String,
                score = (data["score"] as Long).toInt()
            )
        }
    }
    
    fun toDataMap(): Map<String, Any?> {
        return mapOf(
            "locationId" to locationId,
            "userId" to userId,
            "score" to score
        )
    }
}
