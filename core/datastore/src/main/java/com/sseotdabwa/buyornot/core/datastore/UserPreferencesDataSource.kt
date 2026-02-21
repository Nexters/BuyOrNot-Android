package com.sseotdabwa.buyornot.core.datastore

import kotlinx.coroutines.flow.Flow

/**
 * 사용자 정보 DataSource
 *
 * 사용자 프로필 및 인증 토큰을 관리합니다.
 */
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
