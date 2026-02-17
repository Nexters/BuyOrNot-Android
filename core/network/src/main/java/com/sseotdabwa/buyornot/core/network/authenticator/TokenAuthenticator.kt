package com.sseotdabwa.buyornot.core.network.authenticator

import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.datastore.UserPreferencesDataSource
import com.sseotdabwa.buyornot.core.network.AuthEvent
import com.sseotdabwa.buyornot.core.network.AuthEventBus
import com.sseotdabwa.buyornot.core.network.api.AuthApiService
import com.sseotdabwa.buyornot.core.network.dto.request.RefreshRequest
import com.sseotdabwa.buyornot.core.network.dto.response.getOrThrow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Named

class TokenAuthenticator @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
    private val authEventBus: AuthEventBus,
    @Named("Reissue") private val authApiService: AuthApiService,
) : Authenticator {
    private val refreshTokenMutex = Mutex()

    private fun retryCount(response: Response): Int {
        var count = 0
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    override fun authenticate(
        route: Route?,
        response: Response,
    ): Request? =
        runBlocking {
            if (response.code != 401) return@runBlocking null

            val maxRetries = 2
            if (retryCount(response) > maxRetries) {
                userPreferencesDataSource.clearTokens()
                authEventBus.emit(AuthEvent.FORCE_LOGOUT)
                return@runBlocking null
            }

            val originalRequestAccessToken =
                runBlocking {
                    userPreferencesDataSource.preferences.first().accessToken
                }

            refreshTokenMutex.withLock {
                val currentAccessToken =
                    runBlocking {
                        userPreferencesDataSource.preferences.first().accessToken
                    }

                // 토큰이 이미 재발급된 경우 (다른 스레드에서 처리)
                if (originalRequestAccessToken != currentAccessToken) {
                    return@runBlocking response
                        .request
                        .newBuilder()
                        .header("Authorization", "Bearer $currentAccessToken")
                        .build()
                }

                // Refresh Token 가져오기
                val refreshToken = userPreferencesDataSource.preferences.first().refreshToken
                if (refreshToken.isEmpty()) {
                    authEventBus.emit(AuthEvent.FORCE_LOGOUT)
                    return@runBlocking null
                }

                // 토큰 재발급 API 호출 및 처리
                runCatchingCancellable {
                    val newTokens = authApiService.refreshToken(RefreshRequest(refreshToken)).getOrThrow()
                    userPreferencesDataSource.updateTokens(
                        accessToken = newTokens.accessToken,
                        refreshToken = newTokens.refreshToken,
                    )
                    response
                        .request
                        .newBuilder()
                        .header("Authorization", "Bearer ${newTokens.accessToken}")
                        .build()
                }.getOrElse {
                    userPreferencesDataSource.clearTokens()
                    authEventBus.emit(AuthEvent.FORCE_LOGOUT)
                    null
                }
            }
        }
}
