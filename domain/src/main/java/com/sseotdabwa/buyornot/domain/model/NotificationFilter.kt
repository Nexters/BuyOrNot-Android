package com.sseotdabwa.buyornot.domain.model

/**
 * 알림함 필터 칩. 마케팅처럼 어느 칩에도 속하지 않는 알림은 [ALL]에만 보인다.
 */
enum class NotificationFilter {
    ALL,
    MY_VOTE,
    PARTICIPATED,
    COMMENT,
    ;

    fun matches(type: NotificationType): Boolean = this == ALL || this == categoryOf(type)

    companion object {
        fun categoryOf(type: NotificationType): NotificationFilter? =
            when (type) {
                NotificationType.MY_FEED_VOTED_1,
                NotificationType.MY_FEED_VOTED_10,
                NotificationType.MY_FEED_CLOSED,
                -> MY_VOTE

                NotificationType.PARTICIPATED_FEED_CLOSED -> PARTICIPATED
                NotificationType.COMMENT_REPLIED -> COMMENT
                else -> null
            }
    }
}
