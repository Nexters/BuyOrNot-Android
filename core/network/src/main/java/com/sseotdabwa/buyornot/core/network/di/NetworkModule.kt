package com.sseotdabwa.buyornot.core.network.di

import com.sseotdabwa.buyornot.core.datastore.UserPreferencesDataSource
import com.sseotdabwa.buyornot.core.network.AuthEventBus
import com.sseotdabwa.buyornot.core.network.BuildConfig
import com.sseotdabwa.buyornot.core.network.api.AuthApiService
import com.sseotdabwa.buyornot.core.network.api.FeedApiService
import com.sseotdabwa.buyornot.core.network.api.NotificationApiService
import com.sseotdabwa.buyornot.core.network.api.UserApiService
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
    private const val BASE_URL = BuildConfig.BASE_URL

    @Provides
    @Singleton
    fun provideJson(): Json =
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            prettyPrint = true
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

    // 토큰 갱신용 (Interceptor 없이 로깅만 사용)
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
    fun provideUserApiService(
        @Named("AuthClient") okHttpClient: OkHttpClient,
        json: Json,
    ): UserApiService =
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(UserApiService::class.java)

    @Provides
    @Singleton
    fun provideFeedApiService(
        @Named("AuthClient") okHttpClient: OkHttpClient,
        json: Json,
    ): FeedApiService =
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(FeedApiService::class.java)

    @Provides
    @Singleton
    fun provideNotificationApiService(
        @Named("AuthClient") okHttpClient: OkHttpClient,
        json: Json,
    ): NotificationApiService =
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(NotificationApiService::class.java)

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
