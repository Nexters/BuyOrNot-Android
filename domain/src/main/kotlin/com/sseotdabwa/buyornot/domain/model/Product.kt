package com.sseotdabwa.buyornot.domain.model

data class Product(
    val id: String,
    val title: String,
    val description: String,
    val price: Int,
    val imageUrl: String,
)
