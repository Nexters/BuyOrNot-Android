package com.sseotdabwa.buyornot.domain.model

data class Notification(
    val notificationId: Long,
    val feedId: Long,
    val type: NotificationType,
    val title: String,
    val body: String,
    val isRead: Boolean,
    val voteClosedAt: String,
    val resultPercent: Int,
    val resultLabel: String,
    val viewUrl: String,
    val feedTitle: String,
)

enum class NotificationType {
    MY_FEED_VOTED_1,
    MY_FEED_VOTED_10,
    MY_FEED_CLOSED,
    PARTICIPATED_FEED_CLOSED,
    MARKETING_NO_VOTE,
    MARKETING_INACTIVE_3D,
    MARKETING_INACTIVE_7D,
    MARKETING_ONBOARDING_INACTIVE,
    UNKNOWN,
    ;

    companion object {
        fun fromString(type: String): NotificationType = entries.find { it.name == type } ?: UNKNOWN
    }
}
