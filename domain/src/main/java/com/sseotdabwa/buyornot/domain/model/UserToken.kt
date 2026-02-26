package com.sseotdabwa.buyornot.domain.model

/**
 * 인증 토큰 정보 도메인 모델
 */
data class UserToken(
    val accessToken: String,
    val refreshToken: String,
)
