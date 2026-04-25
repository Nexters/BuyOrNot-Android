package com.sseotdabwa.buyornot.core.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class NotificationResponse(
    val notificationId: Long,
    val feedId: Long,
    val type: String,
    val title: String,
    val body: String,
    val isRead: Boolean,
    val voteClosedAt: String,
    val resultPercent: Int,
    val resultLabel: String,
    val viewUrl: String,
    val feedTitle: String?,
)
