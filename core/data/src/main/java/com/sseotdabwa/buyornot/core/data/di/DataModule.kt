package com.sseotdabwa.buyornot.core.data.di

import com.sseotdabwa.buyornot.core.data.repository.AppPreferencesRepositoryImpl
import com.sseotdabwa.buyornot.core.data.repository.AuthRepositoryImpl
import com.sseotdabwa.buyornot.core.data.repository.FeedRepositoryImpl
import com.sseotdabwa.buyornot.core.data.repository.NotificationRepositoryImpl
import com.sseotdabwa.buyornot.core.data.repository.UserPreferencesRepositoryImpl
import com.sseotdabwa.buyornot.core.data.repository.UserRepositoryImpl
import com.sseotdabwa.buyornot.domain.repository.AppPreferencesRepository
import com.sseotdabwa.buyornot.domain.repository.AuthRepository
import com.sseotdabwa.buyornot.domain.repository.FeedRepository
import com.sseotdabwa.buyornot.domain.repository.NotificationRepository
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import com.sseotdabwa.buyornot.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun bindFeedRepository(impl: FeedRepositoryImpl): FeedRepository

    @Binds
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository

    @Binds
    abstract fun bindAppPreferencesRepository(impl: AppPreferencesRepositoryImpl): AppPreferencesRepository
}
