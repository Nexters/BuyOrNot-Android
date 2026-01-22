package com.sseotdabwa.buyornot.core.network.datasource

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeProductNetworkDataSource @Inject constructor() : ProductNetworkDataSource {
    override suspend fun fetchProducts(): List<ProductDto> = listOf(
        ProductDto(
            id = "1",
            title = "최신 패션",
            description = "다가오는 시즌을 위한 필수 아이템",
            price = 79000,
            imageUrl = "https://picsum.photos/200/200?1"
        ),
        ProductDto(
            id = "2",
            title = "미니멀 데일리 백",
            description = "가볍고 실용적인 디자인",
            price = 129000,
            imageUrl = "https://picsum.photos/200/200?2"
        ),
        ProductDto(
            id = "3",
            title = "러닝화",
            description = "안정감 있는 쿠셔닝",
            price = 99000,
            imageUrl = "https://picsum.photos/200/200?3"
        )
    )
}
