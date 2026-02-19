package com.sseotdabwa.buyornot.core.datastore

import kotlinx.coroutines.flow.Flow

interface UserPreferencesDataSource {
    val preferences: Flow<UserPreferences>

    val accessToken: Flow<String>

    val userType: Flow<UserType>

    suspend fun updateDisplayName(newName: String)

    suspend fun updateTokens(
        accessToken: String,
        refreshToken: String,
    )

    suspend fun updateUserType(userType: UserType)

    suspend fun clearTokens()
}
