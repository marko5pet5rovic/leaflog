package com.markopetrovic.leaflog.data.models

data class PlantDTO(
    override val id: String = "",
    override val name: String = "",
    override val description: String = "",
    override val latitude: Double = 0.0,
    override val longitude: Double = 0.0,
    override val points: Int = 0,

    val scientificName: String = "",
    val careTips: String = "",
    val imageUrl: String? = null,

    override val type: String = LocationType.PLANT.typeName,

    val userId: String = ""
) : LocationBase