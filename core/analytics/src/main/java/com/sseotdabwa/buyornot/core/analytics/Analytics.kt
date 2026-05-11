package com.sseotdabwa.buyornot.core.analytics

interface Analytics {
    fun track(event: AnalyticsEvent)

    fun identify(userId: String?)
}
