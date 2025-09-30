data class LeafPlantDTO(
    val id: String,
    val commonName: String,
    val scientificName: String?,
    val description: String?,
    val careTips: List<String>,
    val imageUrl: String,
    val category: String,
    val rarityScore: Int
) {
    companion object {
        fun fromDataMap(data: Map<String, Any>): LeafPlantDTO {
            val rawCareTips = data["careTips"] as? List<*>
            val safeCareTips: List<String> = rawCareTips?.filterIsInstance<String>() ?: emptyList()
            
            return LeafPlantDTO(
                id = data["id"] as String,
                commonName = data["commonName"] as String,
                scientificName = data["scientificName"] as? String,
                description = data["description"] as? String,
                careTips = safeCareTips,
                imageUrl = data["imageUrl"] as String,
                category = data["category"] as String,
                rarityScore = (data["rarityScore"] as Long).toInt()
            )
        }
    }

    fun toDataMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "commonName" to commonName,
            "scientificName" to scientificName,
            "description" to description,
            "careTips" to careTips,
            "imageUrl" to imageUrl,
            "category" to category,
            "rarityScore" to rarityScore
        )
    }
}
