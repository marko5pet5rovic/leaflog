class LeafUserRepository(
    private val dataSource: RealFirebaseDatabase
) { 
    suspend fun getUserByUid(uid: String): LeafAppUserDTO? {
        val rawData = dataSource.getDocument("users", uid) 
        return rawData?.let { data -> LeafAppUserDTO.fromDataMap(data) }
    }
    
    suspend fun registerUser(user: LeafAppUserDTO) {
        dataSource.setDocument("users", user.uid, user.toDataMap())
    }
    
    suspend fun getLeaderboard(limit: Int): List<LeafAppUserDTO> {
        val rawList = dataSource.getRankedUsers(limit)
        return rawList.map { data -> LeafAppUserDTO.fromDataMap(data) }
    }
    
    suspend fun addPointsToUser(uid: String, points: Int) {
        dataSource.incrementPoints(uid, points) 
    }
    
    suspend fun updateProfile(user: LeafAppUserDTO) {
        dataSource.setDocument("users", user.uid, user.toDataMap())
    }
}
