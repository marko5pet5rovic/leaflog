class LeafInteractionRepository(
    private val dataSource: RealFirebaseDatabase
) {
    
    suspend fun addRating(rating: LeafRatingDTO) {
        val documentId = "${rating.userId}_${rating.locationId}" 
        dataSource.setDocument("ratings", documentId, rating.toDataMap())
    }

    suspend fun getRatingsForLocation(locationId: String): List<LeafRatingDTO> {
        val rawList = dataSource.queryDocuments("ratings", "locationId", locationId)
        return rawList.map { data -> LeafRatingDTO.fromDataMap(data) }
    }
}
