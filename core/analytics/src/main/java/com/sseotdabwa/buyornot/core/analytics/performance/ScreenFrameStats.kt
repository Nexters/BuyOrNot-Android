package com.sseotdabwa.buyornot.core.analytics.performance

/**
 * 한 화면에 머무는 동안의 프레임 품질 집계.
 *
 * JankStats 콜백에서 **매 프레임** 호출되므로 [record] 는 할당을 하지 않는다.
 * 메인 스레드에서만 접근한다 — 프레임 콜백과 네비게이션 변경이 모두 메인 스레드다.
 */
class ScreenFrameStats {
    var totalFrames: Long = 0L
        private set

    var jankFrames: Long = 0L
        private set

    var frozenFrames: Long = 0L
        private set

    val isEmpty: Boolean get() = totalFrames == 0L

    fun record(
        isJank: Boolean,
        frameDurationNanos: Long,
    ) {
        totalFrames++
        if (isJank) jankFrames++
        if (frameDurationNanos >= FROZEN_FRAME_THRESHOLD_NANOS) frozenFrames++
    }

    fun reset() {
        totalFrames = 0L
        jankFrames = 0L
        frozenFrames = 0L
    }

    companion object {
        /** Firebase 자동 화면 Trace가 frozen frame으로 세는 기준과 같은 700ms. */
        const val FROZEN_FRAME_THRESHOLD_NANOS: Long = 700_000_000L
    }
}
