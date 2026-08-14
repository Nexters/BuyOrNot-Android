package com.sseotdabwa.buyornot.core.analytics

sealed class AnalyticsEvent {
    data class FeedViewed(
        val firstVisibleItemIndex: Int,
    ) : AnalyticsEvent()

    data class FeedExited(
        val timeSpentSeconds: Float,
        val lastVisibleItemIndex: Int,
    ) : AnalyticsEvent()

    data class VoteSubmitted(
        val feedId: Long,
        val voteChoice: String,
        val feedCategory: String,
    ) : AnalyticsEvent()

    data class VoteCreateStarted(
        val entrySource: String,
        val isLoggedIn: Boolean,
    ) : AnalyticsEvent()

    data class VoteCreateCompleted(
        val itemId: Long,
        val voteTitle: String,
        val optionCount: Int,
    ) : AnalyticsEvent()

    data class VoteCreateAbandoned(
        val filledFields: List<String>,
        val lastStep: String?,
    ) : AnalyticsEvent()

    /**
     * 푸시 알림 탭. 유입 분석의 시작점이며 전환은 Mixpanel Funnel의 시간 순서로 잇는다.
     *
     * 마케팅 알림은 [feedId]·[notificationId]가 없으므로 null로 들어오고,
     * 이때는 Mixpanel 속성 자체를 생략한다.
     */
    data class PushOpened(
        val pushType: String,
        val feedId: Long?,
        val notificationId: Long?,
    ) : AnalyticsEvent()
}
