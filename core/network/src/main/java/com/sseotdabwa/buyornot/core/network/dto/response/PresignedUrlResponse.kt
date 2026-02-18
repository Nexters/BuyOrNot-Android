package com.sseotdabwa.buyornot.core.network.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PresignedUrlResponse(
    @SerialName("uploadUrl")
    val uploadUrl: String,
    @SerialName("s3ObjectKey")
    val s3ObjectKey: String,
    @SerialName("viewUrl")
    val viewUrl: String,
)
