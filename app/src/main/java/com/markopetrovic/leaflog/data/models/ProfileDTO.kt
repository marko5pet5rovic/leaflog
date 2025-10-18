package com.markopetrovic.leaflog.data.models

data class ProfileDTO(
    val uid: String = "",
    val username: String = "N/A",
    val email: String = "N/A",

    val avatarUrl: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,

    val totalPoints: Int = 0,
    val badgesEarned: Int = 0,
)