package com.sseotdabwa.buyornot.core.data.di

import com.sseotdabwa.buyornot.core.data.datasource.RemoteConfigDataSource
import com.sseotdabwa.buyornot.core.data.datasource.RemoteConfigDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RemoteConfigModule {
    @Binds
    abstract fun bindRemoteConfigDataSource(impl: RemoteConfigDataSourceImpl): RemoteConfigDataSource
}
