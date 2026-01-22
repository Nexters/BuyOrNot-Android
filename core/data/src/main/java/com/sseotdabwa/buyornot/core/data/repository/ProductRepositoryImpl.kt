package com.sseotdabwa.buyornot.core.data.repository

import com.sseotdabwa.buyornot.core.network.datasource.ProductNetworkDataSource
import com.sseotdabwa.buyornot.core.network.datasource.ProductDto
import com.sseotdabwa.buyornot.domain.model.Product
import com.sseotdabwa.buyornot.domain.repository.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val networkDataSource: ProductNetworkDataSource
) : ProductRepository {
    override fun observeProducts(): Flow<List<Product>> = flow {
        val products = networkDataSource.fetchProducts().map(ProductDto::toDomain)
        emit(products)
    }
}

private fun ProductDto.toDomain() = Product(
    id = id,
    title = title,
    description = description,
    price = price,
    imageUrl = imageUrl
)
