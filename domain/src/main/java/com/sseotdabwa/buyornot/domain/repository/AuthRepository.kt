package com.sseotdabwa.buyornot.domain.repository

interface AuthRepository {
    suspend fun googleLogin(idToken: String): Result<Unit>

    suspend fun kakaoLogin(accessToken: String): Result<Unit>

    suspend fun logout(): Result<Unit>

    suspend fun clearTokens()
}
