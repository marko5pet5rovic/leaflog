class LeafLocationRepository(
    public val dataSource: RealFirebaseDatabase
) {
    
    suspend fun getLocationById(locationId: String): LeafLocationDTO? {
        val rawData = dataSource.getDocument("locations", locationId)
        return rawData?.let { LeafLocationDTO.fromDataMap(it) }
    }

    suspend fun addRarePlantSpot(spot: LeafRarePlantSpotDTO): String {
        val newDocRef = dataSource.firestore.collection("locations").document()
        val newId = newDocRef.id
        
        dataSource.setDocument("locations", newId, spot.toDataMap())
        return newId
    }
    
    suspend fun addPark(park: LeafParkDTO): String {
        val newDocRef = dataSource.firestore.collection("locations").document()
        val newId = newDocRef.id
        dataSource.setDocument("locations", newId, park.toDataMap())
        return newId
    }
    
    suspend fun addPlantingSpot(spot: LeafPlantingSpotDTO): String {
        val newDocRef = dataSource.firestore.collection("locations").document()
        val newId = newDocRef.id
        dataSource.setDocument("locations", newId, spot.toDataMap())
        return newId
    }

    suspend fun getLocationsInRadius(
        latitude: Double, 
        longitude: Double, 
        radiusInKm: Double
    ): List<LeafLocationDTO> {
        val rawList = dataSource.performGeoQuery(latitude, longitude, radiusInKm)
        return rawList.map { data -> LeafLocationDTO.fromDataMap(data) }
    }
    
    suspend fun searchLocations(
        query: String, 
        type: LocationType?, 
        minRating: Double?, 
    ): List<LeafLocationDTO> {
        val rawList = dataSource.queryDocuments("locations", "title", query)
        return rawList.map { data -> LeafLocationDTO.fromDataMap(data) }
    }

    suspend fun updateLocation(location: LeafLocationDTO) {
        dataSource.setDocument("locations", location.id, location.toDataMap())
    }
    
    suspend fun deleteLocation(locationId: String) {
        dataSource.deleteDocument("locations", locationId)
    }
}
