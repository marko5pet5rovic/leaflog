data class LeafAppUserDTO(
    val uid: String,
    val username: String,
    val email: String,
    val profileImageUrl: String?,
    val points: Int,
    val rank: Int?,
    val joinedTimestamp: Long,
    val addedLocationsCount: Int,
    val badges: List<String>
) {
    companion object {
        fun fromDataMap(data: Map<String, Any>): LeafAppUserDTO {
            val rawBadges = data["badges"] as? List<*>
            val safeBadges: List<String> = rawBadges?.filterIsInstance<String>() ?: emptyList()
            
            return LeafAppUserDTO(
                uid = data["uid"] as String,
                username = data["username"] as String,
                email = data["email"] as String,
                profileImageUrl = data["profileImageUrl"] as? String,
                points = (data["points"] as Long).toInt(),
                rank = (data["rank"] as? Long)?.toInt(),
                joinedTimestamp = data["joinedTimestamp"] as Long,
                addedLocationsCount = (data["addedLocationsCount"] as Long).toInt(),
                badges = safeBadges
            )
        }
    }

    fun toDataMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "username" to username,
            "email" to email,
            "profileImageUrl" to profileImageUrl,
            "points" to points,
            "rank" to rank,
            "joinedTimestamp" to joinedTimestamp,
            "addedLocationsCount" to addedLocationsCount,
            "badges" to badges
        )
    }
}
