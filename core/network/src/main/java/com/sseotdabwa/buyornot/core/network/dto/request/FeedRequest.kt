package com.sseotdabwa.buyornot.core.network.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedRequest(
    @SerialName("category")
    val category: String,
    @SerialName("price")
    val price: Int,
    @SerialName("content")
    val content: String,
    @SerialName("s3ObjectKey")
    val s3ObjectKey: String,
    @SerialName("imageWidth")
    val imageWidth: Int,
    @SerialName("imageHeight")
    val imageHeight: Int,
)
