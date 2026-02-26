package com.sseotdabwa.buyornot.domain.model

/**
 * 사용자 정보 도메인 모델
 */
data class UserPreferences(
    val displayName: String,
    val profileImageUrl: String,
    val userType: UserType,
)
