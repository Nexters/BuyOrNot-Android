package com.sseotdabwa.buyornot.core.data.repository

import com.sseotdabwa.buyornot.core.datastore.UserPreferencesDataSource
import com.sseotdabwa.buyornot.core.network.api.AuthApiService
import com.sseotdabwa.buyornot.core.network.dto.request.GoogleLoginRequest
import com.sseotdabwa.buyornot.core.network.dto.request.KakaoLoginRequest
import com.sseotdabwa.buyornot.core.network.dto.request.RefreshRequest
import com.sseotdabwa.buyornot.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val userPreferencesDataSource: UserPreferencesDataSource,
) : AuthRepository {
    override suspend fun googleLogin(idToken: String) {
        val response = authApiService.googleLogin(GoogleLoginRequest(idToken))
        userPreferencesDataSource.updateTokens(
            accessToken = response.data.accessToken,
            refreshToken = response.data.refreshToken,
        )
    }

    override suspend fun kakaoLogin(accessToken: String) {
        val response = authApiService.kakaoLogin(KakaoLoginRequest(accessToken))
        userPreferencesDataSource.updateTokens(
            accessToken = response.data.accessToken,
            refreshToken = response.data.refreshToken,
        )
    }

    override suspend fun logout() {
        val refreshToken = userPreferencesDataSource.preferences.first().refreshToken
        authApiService.logout(RefreshRequest(refreshToken))
    }

    override suspend fun clearTokens() {
        userPreferencesDataSource.clearTokens()
    }
}
