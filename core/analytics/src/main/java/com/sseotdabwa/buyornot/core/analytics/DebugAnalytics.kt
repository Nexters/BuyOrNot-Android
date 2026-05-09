package com.sseotdabwa.buyornot.core.analytics

import android.util.Log

class DebugAnalytics : Analytics {
    override fun track(event: AnalyticsEvent) {
        Log.d("Analytics", event.toString())
    }
}
