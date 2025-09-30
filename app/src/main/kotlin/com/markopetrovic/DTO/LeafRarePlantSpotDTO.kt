data class LeafRarePlantSpotDTO(
    val location: LeafLocationDTO,
    val plantDetails: LeafPlantDTO,
    val specificPlantCount: Int?
) {
    companion object {
        fun fromDataMap(data: Map<String, Any>): LeafRarePlantSpotDTO {
            
            val rawLocationMap = data["location"] as? Map<*, *>
            val rawPlantDetailsMap = data["plantDetails"] as? Map<*, *>

            requireNotNull(rawLocationMap) { "Location data is missing or invalid." }
            requireNotNull(rawPlantDetailsMap) { "Plant details data is missing or invalid." }

            @Suppress("UNCHECKED_CAST")
            return LeafRarePlantSpotDTO(
                location = LeafLocationDTO.fromDataMap(rawLocationMap as Map<String, Any>),
                plantDetails = LeafPlantDTO.fromDataMap(rawPlantDetailsMap as Map<String, Any>),
                specificPlantCount = (data["specificPlantCount"] as? Long)?.toInt()
            )
        }
    }

    fun toDataMap(): Map<String, Any?> {
        return mapOf(
            "location" to location.toDataMap(),
            "plantDetails" to plantDetails.toDataMap(),
            "specificPlantCount" to specificPlantCount
        )
    }
}
