package com.sseotdabwa.buyornot.domain.repository

import com.sseotdabwa.buyornot.domain.model.UserPreferences
import com.sseotdabwa.buyornot.domain.model.UserToken
import com.sseotdabwa.buyornot.domain.model.UserType
import kotlinx.coroutines.flow.Flow

/**
 * 사용자 정보 Repository
 */
interface UserPreferencesRepository {
    /**
     * 전체 사용자 정보를 Flow로 제공
     */
    val userPreferences: Flow<UserPreferences>

    /**
     * 인증 토큰 정보를 Flow로 제공
     */
    val userToken: Flow<UserToken>

    /**
     * 현재 사용자 타입을 Flow로 제공
     */
    val userType: Flow<UserType>

    /**
     * 사용자 타입 업데이트
     */
    suspend fun updateUserType(userType: UserType)

    /**
     * 표시 이름 업데이트
     */
    suspend fun updateDisplayName(newName: String)

    /**
     * 프로필 이미지 URL 업데이트
     */
    suspend fun updateProfileImageUrl(newUrl: String)
}
