package com.markopetrovic.leaflog.data.models

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class PlantingSpotDTO(
    @get:Exclude override val id: String = "",
    override val name: String = "",
    override val description: String = "",
    override val latitude: Double = 0.0,
    override val longitude: Double = 0.0,
    override val points: Int = 0,

    @get:PropertyName("typeString")
    override val typeString: String = LocationType.PLANTING_SPOT.typeName,

    @get:PropertyName("fenced")
    val isFenced: Boolean = false,

    val soilType: String = "",

    val userId: String = ""
) : LocationBase