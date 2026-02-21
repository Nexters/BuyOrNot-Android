package com.sseotdabwa.buyornot.core.datastore.di

import com.sseotdabwa.buyornot.core.datastore.AppPreferencesDataSource
import com.sseotdabwa.buyornot.core.datastore.AppPreferencesDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * AppPreferencesDataSource Hilt 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppPreferencesDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindAppPreferencesDataSource(
        impl: AppPreferencesDataSourceImpl,
    ): AppPreferencesDataSource
}

