package com.sseotdabwa.buyornot.core.network.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentRequest(
    @SerialName("content")
    val content: String,
)
