package com.sseotdabwa.buyornot.core.data.repository

import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.datastore.UserPreferencesDataSource
import com.sseotdabwa.buyornot.core.network.api.AuthApiService
import com.sseotdabwa.buyornot.core.network.dto.request.GoogleLoginRequest
import com.sseotdabwa.buyornot.core.network.dto.request.KakaoLoginRequest
import com.sseotdabwa.buyornot.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val userPreferencesDataSource: UserPreferencesDataSource,
) : AuthRepository {
    override suspend fun googleLogin(idToken: String): Result<Unit> =
        runCatchingCancellable {
            val response = authApiService.googleLogin(GoogleLoginRequest(idToken))
            userPreferencesDataSource.updateTokens(
                accessToken = response.data.accessToken,
                refreshToken = response.data.refreshToken,
            )
        }

    override suspend fun kakaoLogin(accessToken: String): Result<Unit> =
        runCatchingCancellable {
            val response = authApiService.kakaoLogin(KakaoLoginRequest(accessToken))
            userPreferencesDataSource.updateTokens(
                accessToken = response.data.accessToken,
                refreshToken = response.data.refreshToken,
            )
        }

    override suspend fun clearTokens() {
        userPreferencesDataSource.clearTokens()
    }
}
