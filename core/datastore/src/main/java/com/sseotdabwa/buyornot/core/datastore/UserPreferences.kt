package com.sseotdabwa.buyornot.core.datastore

/**
 * 사용자 유형 열거형
 */
enum class UserType {
    GUEST, // 비회원
    SOCIAL, // 소셜 로그인 (구글/카카오)
}

data class UserPreferences(
    val displayName: String = "손님",
    val accessToken: String = "",
    val refreshToken: String = "",
    val userType: UserType = UserType.GUEST,
)
