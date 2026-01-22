package com.sseotdabwa.buyornot.core.network.datasource

interface ProductNetworkDataSource {
    suspend fun fetchProducts(): List<ProductDto>
}
