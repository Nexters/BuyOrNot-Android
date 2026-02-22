package com.sseotdabwa.buyornot.core.network.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class FcmTokenRequest(
    val fcmToken: String,
)
