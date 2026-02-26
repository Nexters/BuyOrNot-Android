package com.sseotdabwa.buyornot.core.data.repository

import com.sseotdabwa.buyornot.core.datastore.UserPreferencesDataSource
import com.sseotdabwa.buyornot.core.datastore.UserType
import com.sseotdabwa.buyornot.core.network.api.AuthApiService
import com.sseotdabwa.buyornot.core.network.dto.request.GoogleLoginRequest
import com.sseotdabwa.buyornot.core.network.dto.request.KakaoLoginRequest
import com.sseotdabwa.buyornot.core.network.dto.request.RefreshRequest
import com.sseotdabwa.buyornot.core.network.dto.response.getOrThrow
import com.sseotdabwa.buyornot.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 인증 관련 비즈니스 로직을 처리하는 Repository 구현체
 *
 * AuthApiService를 통해 서버와 통신하고,
 * UserPreferencesDataSource를 통해 토큰과 사용자 타입을 로컬에 저장합니다.
 */
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val userPreferencesDataSource: UserPreferencesDataSource,
) : AuthRepository {
    override suspend fun googleLogin(idToken: String) {
        val tokenData = authApiService.googleLogin(GoogleLoginRequest(idToken)).getOrThrow()
        userPreferencesDataSource.updateTokens(
            accessToken = tokenData.accessToken,
            refreshToken = tokenData.refreshToken,
        )
        userPreferencesDataSource.updateUserType(UserType.SOCIAL)
    }

    override suspend fun kakaoLogin(accessToken: String) {
        val tokenData = authApiService.kakaoLogin(KakaoLoginRequest(accessToken)).getOrThrow()
        userPreferencesDataSource.updateTokens(
            accessToken = tokenData.accessToken,
            refreshToken = tokenData.refreshToken,
        )
        userPreferencesDataSource.updateUserType(UserType.SOCIAL)
    }

    override suspend fun logout() {
        val refreshToken = userPreferencesDataSource.preferences.first().refreshToken
        authApiService.logout(RefreshRequest(refreshToken)).getOrThrow()
    }

    override suspend fun clearUserInfo() {
        userPreferencesDataSource.clearUserInfo()
    }
}
