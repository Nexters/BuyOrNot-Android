package com.sseotdabwa.buyornot.domain.repository

import com.sseotdabwa.buyornot.domain.model.UserType
import kotlinx.coroutines.flow.Flow

/**
 * 사용자 정보 Repository
 */
interface UserPreferencesRepository {
    /**
     * 현재 사용자 타입을 Flow로 제공
     */
    val userType: Flow<UserType>

    /**
     * 사용자 타입 업데이트
     */
    suspend fun updateUserType(userType: UserType)
}
