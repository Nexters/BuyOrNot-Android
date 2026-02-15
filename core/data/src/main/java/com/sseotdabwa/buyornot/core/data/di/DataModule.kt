package com.sseotdabwa.buyornot.core.data.di

import com.sseotdabwa.buyornot.core.data.repository.AuthRepositoryImpl
import com.sseotdabwa.buyornot.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
