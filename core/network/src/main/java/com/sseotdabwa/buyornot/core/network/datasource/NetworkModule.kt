package com.sseotdabwa.buyornot.core.network.datasource

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class NetworkModule {
    @Binds
    abstract fun bindProductNetworkDataSource(impl: FakeProductNetworkDataSource): ProductNetworkDataSource
}
