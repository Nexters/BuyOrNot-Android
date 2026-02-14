package com.sseotdabwa.buyornot.core.network.authenticator

import com.sseotdabwa.buyornot.core.datastore.UserPreferencesDataSource
import com.sseotdabwa.buyornot.core.network.AuthEvent
import com.sseotdabwa.buyornot.core.network.AuthEventBus
import com.sseotdabwa.buyornot.core.network.api.AuthApiService
import com.sseotdabwa.buyornot.core.network.dto.request.RefreshRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Named

class TokenAuthenticator
    @Inject
    constructor(
        private val userPreferencesDataSource: UserPreferencesDataSource,
        private val authEventBus: AuthEventBus,
        @Named("Reissue") private val authApiService: AuthApiService,
    ) : Authenticator {
        override fun authenticate(
            route: Route?,
            response: Response,
        ): Request? {
            // 1. Refresh Token 가져오기
            val refreshToken = runBlocking { userPreferencesDataSource.preferences.first().refreshToken }
            if (refreshToken.isEmpty()) {
                runBlocking { authEventBus.emit(AuthEvent.FORCE_LOGOUT) }
                return null
            }

            // 2. 토큰 재발급 API 호출
            val newTokensResult =
                runBlocking {
                    runCatching {
                        authApiService.refreshToken(RefreshRequest(refreshToken))
                    }
                }

            return if (newTokensResult.isSuccess) {
                // 3. 재발급 성공
                val newTokens = newTokensResult.getOrThrow().data
                runBlocking {
                    userPreferencesDataSource.updateTokens(
                        accessToken = newTokens.accessToken,
                        refreshToken = newTokens.refreshToken,
                    )
                }
                response
                    .request
                    .newBuilder()
                    .header("Authorization", "Bearer ${newTokens.accessToken}")
                    .build()
            } else {
                // 4. 재발급 실패
                runBlocking {
                    userPreferencesDataSource.clearTokens()
                    authEventBus.emit(AuthEvent.FORCE_LOGOUT)
                }
                null
            }
        }
    }
