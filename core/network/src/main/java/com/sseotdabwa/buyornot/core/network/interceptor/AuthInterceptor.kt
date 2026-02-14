package com.sseotdabwa.buyornot.core.network.interceptor

import com.sseotdabwa.buyornot.core.datastore.UserPreferencesDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = runBlocking { userPreferencesDataSource.accessToken.first() }
        val request =
            chain
                .request()
                .newBuilder()
                .apply {
                    if (accessToken.isNotEmpty()) {
                        addHeader("Authorization", "Bearer $accessToken")
                    }
                }.build()
        return chain.proceed(request)
    }
}
