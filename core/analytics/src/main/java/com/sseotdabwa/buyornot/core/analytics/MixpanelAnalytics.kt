package com.sseotdabwa.buyornot.core.analytics

import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONArray
import org.json.JSONObject

class MixpanelAnalytics(
    private val mixpanel: MixpanelAPI,
    appVersion: String,
) : Analytics {
    init {
        mixpanel.registerSuperProperties(
            JSONObject().apply {
                put("platform", "android")
                put("app_version", appVersion)
                put("user_id", JSONObject.NULL)
            },
        )
    }

    override fun track(event: AnalyticsEvent) {
        val (name, props) = event.toMixpanelEvent()
        mixpanel.track(name, props)
    }

    override fun identify(userId: String?) {
        if (userId != null) {
            mixpanel.identify(userId)
        }
        mixpanel.registerSuperProperties(
            JSONObject().apply { put("user_id", userId ?: JSONObject.NULL) },
        )
    }
}

/**
 * 이벤트를 Mixpanel 이벤트명 + 속성으로 변환한다.
 *
 * [MixpanelAnalytics] 인스턴스 없이 검증할 수 있도록 top-level 함수로 둔다.
 */
internal fun AnalyticsEvent.toMixpanelEvent(): Pair<String, JSONObject> {
    val props = JSONObject()
    val name =
        when (this) {
            is AnalyticsEvent.FeedViewed -> {
                props.put("first_visible_item_index", firstVisibleItemIndex)
                "feed_viewed"
            }
            is AnalyticsEvent.FeedExited -> {
                props.put("time_spent_seconds", timeSpentSeconds)
                props.put("last_visible_item_index", lastVisibleItemIndex)
                "feed_exited"
            }
            is AnalyticsEvent.VoteSubmitted -> {
                props.put("feed_id", feedId)
                props.put("vote_choice", voteChoice)
                props.put("feed_category", feedCategory)
                "vote_submitted"
            }
            is AnalyticsEvent.VoteCreateStarted -> {
                props.put("entry_source", entrySource)
                props.put("is_logged_in", isLoggedIn)
                "vote_create_started"
            }
            is AnalyticsEvent.VoteCreateCompleted -> {
                props.put("item_id", itemId)
                props.put("vote_title", voteTitle)
                props.put("option_count", optionCount)
                "vote_create_completed"
            }
            is AnalyticsEvent.VoteCreateAbandoned -> {
                props.put("filled_fields", JSONArray(filledFields))
                if (lastStep != null) props.put("last_step", lastStep)
                "vote_create_abandoned"
            }
            is AnalyticsEvent.PushOpened -> {
                props.put("push_type", pushType)
                // 마케팅 알림은 feedId·notificationId가 없다. null을 넣지 않고 속성 자체를 생략해
                // Mixpanel에서 "속성 없음"으로 필터링할 수 있게 한다.
                if (feedId != null) props.put("feed_id", feedId)
                if (notificationId != null) props.put("notification_id", notificationId)
                "push_opened"
            }
        }
    return name to props
}
