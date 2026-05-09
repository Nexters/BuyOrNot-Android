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
}
