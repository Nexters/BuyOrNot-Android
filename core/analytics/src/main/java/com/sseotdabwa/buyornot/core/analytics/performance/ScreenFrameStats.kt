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
    }

    companion object {
        /** Firebase 자동 화면 Trace가 frozen frame으로 세는 기준과 같은 700ms. */
        const val FROZEN_FRAME_THRESHOLD_NANOS: Long = 700_000_000L
    }
}
