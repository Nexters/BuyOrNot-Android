package com.sseotdabwa.buyornot.core.datastore

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataStoreModule {
    @Binds
    abstract fun bindUserPreferencesDataSource(impl: UserPreferencesDataSourceImpl): UserPreferencesDataSource

    @Binds
    abstract fun bindAppPreferencesDataSource(impl: AppPreferencesDataSourceImpl): AppPreferencesDataSource
}
