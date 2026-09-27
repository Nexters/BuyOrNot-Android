package com.sseotdabwa.buyornot.core.analytics

import timber.log.Timber

class DebugAnalytics(
    private val appVersion: String,
) : Analytics {
    private var userId: String? = null

    override fun track(event: AnalyticsEvent) {
        val superProps = "platform=android, app_version=$appVersion, user_id=$userId"
        Timber.tag("Analytics").d("$event [$superProps]")
    }

    override fun identify(userId: String?) {
        this.userId = userId
        Timber.tag("Analytics").d("identify: userId=$userId")
    }
}
