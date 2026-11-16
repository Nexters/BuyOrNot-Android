package com.sseotdabwa.buyornot.core.network.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedAiDraftRequest(
    @SerialName("s3ObjectKeys")
    val s3ObjectKeys: List<String>,
)
