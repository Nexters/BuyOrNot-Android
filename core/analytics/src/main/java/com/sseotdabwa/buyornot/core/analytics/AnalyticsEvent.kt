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

    /**
     * 앱 링크(`https://{host}/feed/{feedId}`) 탭으로 앱이 열림.
     *
     * [PushOpened]와 절대 섞지 않는다 — 앱 링크 유입을 푸시 이벤트에 태우면
     * 푸시 CTR과 퍼널 전환율이 통째로 오염된다.
     *
     * 파싱에 실패해도([linkStatus] = `invalid`) 이벤트는 발행한다.
     * 발행을 건너뛰면 «링크가 안 온 것»과 «와서 깨진 것»을 구분할 수 없다.
     */
    data class AppLinkOpened(
        val linkStatus: String,
        val feedId: Long?,
        val referrer: String?,
        val utmSource: String?,
        val utmMedium: String?,
        val utmCampaign: String?,
    ) : AnalyticsEvent() {
        companion object {
            const val STATUS_RESOLVED = "resolved"
            const val STATUS_INVALID = "invalid"
        }
    }
}
