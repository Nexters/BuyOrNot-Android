package com.sseotdabwa.buyornot.core.analytics

import android.util.Log

class DebugAnalytics(
    private val appVersion: String,
) : Analytics {
    private var userId: String? = null

    override fun track(event: AnalyticsEvent) {
        val superProps = "platform=android, app_version=$appVersion, user_id=$userId"
        Log.d("Analytics", "$event [$superProps]")
    }

    override fun identify(userId: String?) {
        this.userId = userId
        Log.d("Analytics", "identify: userId=$userId")
    }
}
