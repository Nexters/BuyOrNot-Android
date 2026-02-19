package com.sseotdabwa.buyornot.core.network.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PresignedUrlRequest(
    @SerialName("fileName")
    val fileName: String,
    @SerialName("contentType")
    val contentType: String,
)
