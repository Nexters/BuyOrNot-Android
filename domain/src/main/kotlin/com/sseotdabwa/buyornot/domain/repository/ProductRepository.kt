package com.sseotdabwa.buyornot.domain.repository

import com.sseotdabwa.buyornot.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeProducts(): Flow<List<Product>>
}
