package com.sseotdabwa.buyornot.core.analytics.performance

/**
 * 한 화면에 머무는 동안의 프레임 품질 집계.
 *
 * [record] 는 JankStats 콜백에서 **매 프레임** 호출된다. 이 콜백은 API 24+ 에서 메인 스레드가
 * 아니라 플랫폼 FrameMetrics 전용 스레드로 온다. 반면 [snapshot] 과 [reset] 은 네비게이션·
 * 수명주기를 따라 메인 스레드에서 호출된다. 그래서 모든 접근을 같은 락으로 묶는다.
 * 두 스레드가 부딪히는 일이 거의 없어 프레임 경로 비용은 무시할 수 있고, [record] 는 할당을 하지 않는다.
 */
class ScreenFrameStats {
    private val lock = Any()

    private var totalFrames: Long = 0L
    private var jankFrames: Long = 0L
    private var frozenFrames: Long = 0L

    /**
     * 프레임 한 장을 센다. JankStats 의 `FrameData` 는 다음 프레임에 재사용되므로
     * 호출부에서 필요한 값만 뽑아 넘기고, 여기서도 인스턴스를 보관하지 않는다.
     */
    fun record(
        isJank: Boolean,
        frameDurationNanos: Long,
    ) = synchronized(lock) {
        totalFrames++
        if (isJank) jankFrames++
        if (frameDurationNanos >= FROZEN_FRAME_THRESHOLD_NANOS) frozenFrames++
    }

    /** 세 값이 서로 어긋난 시점의 것으로 섞이지 않도록 한 번에 읽는다. */
    fun snapshot(): Snapshot =
        synchronized(lock) {
            Snapshot(
                totalFrames = totalFrames,
                jankFrames = jankFrames,
                frozenFrames = frozenFrames,
            )
        }

    fun reset() =
        synchronized(lock) {
            totalFrames = 0L
            jankFrames = 0L
            frozenFrames = 0L
        }

    /** [snapshot] 이 뜬 한 시점의 집계. */
    data class Snapshot(
        val totalFrames: Long,
        val jankFrames: Long,
        val frozenFrames: Long,
    ) {
        val isEmpty: Boolean get() = totalFrames == 0L

        /**
         * 비율을 지표로 실을 만큼 프레임이 모였는지.
         *
         * 분모가 작으면 비율이 크게 튄다 — 2프레임 중 1 jank는 500‰ 이지만 화면 품질에 대해
         * 아무것도 말해주지 않는다. Firebase는 측정항목을 trace 단위로 **비가중 평균** 하므로
         * 이런 값 하나가 전체 평균을 지배한다. 그래서 짧은 구간은 카운트만 남기고 비율은 뺀다.
         */
        val isRateReliable: Boolean get() = totalFrames >= MIN_FRAMES_FOR_RATE

        /** 전체 프레임 중 jank 프레임 비율 (‰). */
        val jankRatePermille: Long get() = ratePermille(jankFrames)

        /** 전체 프레임 중 frozen 프레임 비율 (‰). */
        val frozenRatePermille: Long get() = ratePermille(frozenFrames)

        /** 정수 지표라 ‰ 로 싣는다. 내림이 아니라 반올림해 작은 비율이 0으로 눌리지 않게 한다. */
        private fun ratePermille(count: Long): Long = if (totalFrames == 0L) 0L else (count * 1_000L + totalFrames / 2) / totalFrames
    }

    companion object {
        /** Firebase 자동 화면 Trace가 frozen frame으로 세는 기준과 같은 700ms. */
        const val FROZEN_FRAME_THRESHOLD_NANOS: Long = 700_000_000L

        /** 비율을 신뢰할 수 있는 최소 프레임 수. 60Hz에서 약 0.5초. */
        const val MIN_FRAMES_FOR_RATE: Long = 30L
    }
}
