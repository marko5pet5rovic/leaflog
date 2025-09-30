data class LeafPlantingSpotDTO(
    val location: LeafLocationDTO,
    val soilType: String,
    val sunExposure: String,
    val waterSourceNearby: Boolean,
    val suitableFor: List<String>
) {
    companion object {
        fun fromDataMap(data: Map<String, Any>): LeafPlantingSpotDTO {
            val rawSuitableFor = data["suitableFor"] as? List<*>
            val safeSuitableFor: List<String> = rawSuitableFor?.filterIsInstance<String>() ?: emptyList()

            @Suppress("UNCHECKED_CAST")
            return LeafPlantingSpotDTO(
                location = LeafLocationDTO.fromDataMap(data["location"] as Map<String, Any>),
                soilType = data["soilType"] as String,
                sunExposure = data["sunExposure"] as String,
                waterSourceNearby = data["waterSourceNearby"] as Boolean,
                suitableFor = safeSuitableFor
            )
        }
    }

    fun toDataMap(): Map<String, Any?> {
        return mapOf(
            "location" to location.toDataMap(),
            "soilType" to soilType,
            "sunExposure" to sunExposure,
            "waterSourceNearby" to waterSourceNearby,
            "suitableFor" to suitableFor
        )
    }
}
