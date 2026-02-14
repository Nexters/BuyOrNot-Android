package com.sseotdabwa.buyornot.core.network.di

import com.sseotdabwa.buyornot.core.datastore.UserPreferencesDataSource
import com.sseotdabwa.buyornot.core.network.AuthEventBus
import com.sseotdabwa.buyornot.core.network.api.AuthApiService
import com.sseotdabwa.buyornot.core.network.authenticator.TokenAuthenticator
import com.sseotdabwa.buyornot.core.network.interceptor.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BASE_URL = "https://dev.buy-or-not.com"

    @Provides
    @Singleton
    fun provideJson(): Json =
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

    // For general API calls that need authentication
    @Provides
    @Singleton
    @Named("AuthClient")
    fun provideAuthOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                },
            ).authenticator(tokenAuthenticator)
            .build()

    // For token reissue calls (to avoid circular dependency)
    @Provides
    @Singleton
    @Named("ReissueClient")
    fun provideReissueOkHttpClient(): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                },
            ).build()

    @Provides
    @Singleton
    fun provideAuthApiService(
        @Named("AuthClient") okHttpClient: OkHttpClient,
        json: Json,
    ): AuthApiService =
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AuthApiService::class.java)

    @Provides
    @Singleton
    @Named("Reissue")
    fun provideReissueAuthApiService(
        @Named("ReissueClient") okHttpClient: OkHttpClient,
        json: Json,
    ): AuthApiService =
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideAuthInterceptor(userPreferencesDataSource: UserPreferencesDataSource): AuthInterceptor =
        AuthInterceptor(userPreferencesDataSource)

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        userPreferencesDataSource: UserPreferencesDataSource,
        authEventBus: AuthEventBus,
        @Named("Reissue") authApiService: AuthApiService,
    ): TokenAuthenticator = TokenAuthenticator(userPreferencesDataSource, authEventBus, authApiService)
}
