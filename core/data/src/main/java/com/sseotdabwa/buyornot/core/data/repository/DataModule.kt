package com.sseotdabwa.buyornot.core.data.repository

import com.sseotdabwa.buyornot.domain.repository.ProductRepository
import com.sseotdabwa.buyornot.domain.repository.UserDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
    @Binds
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds
    abstract fun bindUserDataRepository(impl: UserDataRepositoryImpl): UserDataRepository
}
