package com.sseotdabwa.buyornot.core.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class UnreadCountResponse(
    val unreadCount: Int,
)
