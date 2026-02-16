package com.sseotdabwa.buyornot.core.datastore

data class UserPreferences(
    val displayName: String = "손님",
    val accessToken: String = "",
    val refreshToken: String = "",
)
