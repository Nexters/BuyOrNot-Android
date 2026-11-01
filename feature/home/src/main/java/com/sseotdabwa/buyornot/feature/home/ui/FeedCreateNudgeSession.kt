package com.sseotdabwa.buyornot.feature.home.ui

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 홈 등록 넛지는 세션당 한 번만 띄운다. 세션은 앱 프로세스 수명이라 메모리에만 기록한다.
 */
@Singleton
class FeedCreateNudgeSession @Inject constructor() {
    var hasShown: Boolean = false
        private set

    fun markShown() {
        hasShown = true
    }
}
