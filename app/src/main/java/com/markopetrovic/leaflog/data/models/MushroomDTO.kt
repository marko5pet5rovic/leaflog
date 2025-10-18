package com.markopetrovic.leaflog.data.models

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class MushroomDTO(
    @get:Exclude override val id: String = "",
    override val name: String = "",
    override val description: String = "",
    override val latitude: Double = 0.0,
    override val longitude: Double = 0.0,
    override val points: Int = 0,

    @get:PropertyName("type")
    override val typeString: String = LocationType.MUSHROOM.typeName,

    val imageUrl: String? = null,
    val isEdible: Boolean = false,
    val habitat: String = "",

    val userId: String = ""

) : LocationBase