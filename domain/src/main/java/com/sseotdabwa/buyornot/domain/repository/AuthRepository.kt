package com.sseotdabwa.buyornot.domain.repository

interface AuthRepository {
    suspend fun googleLogin(idToken: String)

    suspend fun kakaoLogin(accessToken: String)

    suspend fun logout()

    suspend fun clearUserInfo()
}
