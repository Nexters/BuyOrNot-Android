package com.sseotdabwa.buyornot.core.datastore

/**
 * 사용자 유형 열거형
 */
enum class UserType {
    GUEST, // 비회원
    SOCIAL, // 소셜 로그인 (구글/카카오)
}

/**
 * 사용자 정보
 *
 * 사용자 프로필 및 인증 토큰을 관리합니다.
 */
data class UserPreferences(
    val displayName: String = "손님",
    val profileImageUrl: String = "",
    val accessToken: String = "",
    val refreshToken: String = "",
    val userType: UserType = UserType.GUEST,
    val isFirstRun: Boolean = true,
)
