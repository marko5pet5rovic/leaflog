data class LeafParkDTO(
    val location: LeafLocationDTO,
    val floraDescription: String,
    val parkAreaSqM: Double?,
    val facilities: List<String>
) {
     companion object {
        fun fromDataMap(data: Map<String, Any>): LeafParkDTO {
            val rawFacilities = data["facilities"] as? List<*>
            val safeFacilities: List<String> = rawFacilities?.filterIsInstance<String>() ?: emptyList()

            @Suppress("UNCHECKED_CAST")
            return LeafParkDTO(
                location = LeafLocationDTO.fromDataMap(data["location"] as Map<String, Any>),
                floraDescription = data["floraDescription"] as String,
                parkAreaSqM = data["parkAreaSqM"] as? Double,
                facilities = safeFacilities
            )
        }
    }

    fun toDataMap(): Map<String, Any?> {
        return mapOf(
            "location" to location.toDataMap(),
            "floraDescription" to floraDescription,
            "parkAreaSqM" to parkAreaSqM,
            "facilities" to facilities
        )
    }
}
