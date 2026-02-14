package com.sseotdabwa.buyornot.core.network.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RefreshRequest(
    val refreshToken: String,
)
