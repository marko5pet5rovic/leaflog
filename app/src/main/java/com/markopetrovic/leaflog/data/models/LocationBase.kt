package com.markopetrovic.leaflog.data.models
enum class LocationType(val typeName: String) {
    PLANT("Plant"),
    MUSHROOM("Mushroom"),
    PLANTING_SPOT("Planting Spot"),
    GENERIC_LOCATION("Generic Location")
}

sealed interface LocationBase {
    val id: String
    val name: String
    val description: String
    val latitude: Double
    val longitude: Double
    val points: Int
    val typeString: String
}