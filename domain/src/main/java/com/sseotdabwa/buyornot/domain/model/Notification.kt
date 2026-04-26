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
    MY_FEED_CLOSED,
    PARTICIPATED_FEED_CLOSED,
    UNKNOWN,
    ;

    companion object {
        fun fromString(type: String): NotificationType = entries.find { it.name == type } ?: UNKNOWN
    }
}
