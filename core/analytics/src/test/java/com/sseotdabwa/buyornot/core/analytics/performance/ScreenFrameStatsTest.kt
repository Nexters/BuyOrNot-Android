package com.sseotdabwa.buyornot.core.analytics.performance

import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScreenFrameStatsTest {
    private val stats = ScreenFrameStats()

    private val smoothFrame = 8_000_000L
    private val jankFrame = 20_000_000L
    private val frozenFrame = ScreenFrameStats.FROZEN_FRAME_THRESHOLD_NANOS

    @Test
    fun `프레임이_없으면_비어_있다`() {
        assertTrue(stats.snapshot().isEmpty)
    }

    @Test
    fun `전체_프레임과_jank_프레임을_따로_센다`() {
        stats.record(isJank = false, frameDurationNanos = smoothFrame)
        stats.record(isJank = true, frameDurationNanos = jankFrame)
        stats.record(isJank = true, frameDurationNanos = jankFrame)

        val snapshot = stats.snapshot()
        assertFalse(snapshot.isEmpty)
        assertEquals(3L, snapshot.totalFrames)
        assertEquals(2L, snapshot.jankFrames)
    }

    @Test
    fun `700ms_이상은_frozen_프레임으로도_센다`() {
        stats.record(isJank = true, frameDurationNanos = frozenFrame)

        val snapshot = stats.snapshot()
        assertEquals(1L, snapshot.jankFrames)
        assertEquals(1L, snapshot.frozenFrames)
    }

    @Test
    fun `700ms_미만은_frozen이_아니다`() {
        stats.record(isJank = true, frameDurationNanos = frozenFrame - 1)

        val snapshot = stats.snapshot()
        assertEquals(1L, snapshot.jankFrames)
        assertEquals(0L, snapshot.frozenFrames)
    }

    @Test
    fun `reset은_이전_화면의_집계를_남기지_않는다`() {
        stats.record(isJank = true, frameDurationNanos = frozenFrame)

        stats.reset()

        val snapshot = stats.snapshot()
        assertTrue(snapshot.isEmpty)
        assertEquals(0L, snapshot.totalFrames)
        assertEquals(0L, snapshot.jankFrames)
        assertEquals(0L, snapshot.frozenFrames)
    }

    /**
     * JankStats 콜백은 API 24+ 에서 메인 스레드가 아닌 프레임 메트릭스 스레드로 온다.
     * 그 스레드의 [ScreenFrameStats.record] 와 메인 스레드의 읽기가 겹쳐도 카운트가 새지 않아야 한다.
     */
    @Test
    fun `다른_스레드에서_동시에_기록해도_프레임_수가_새지_않는다`() {
        val threadCount = 4
        val framesPerThread = 10_000
        val start = CountDownLatch(1)

        val recorders =
            List(threadCount) {
                thread {
                    start.await()
                    repeat(framesPerThread) {
                        stats.record(isJank = true, frameDurationNanos = frozenFrame)
                    }
                }
            }
        start.countDown()
        recorders.forEach { it.join(TimeUnit.SECONDS.toMillis(10)) }

        val snapshot = stats.snapshot()
        val expected = (threadCount * framesPerThread).toLong()
        assertEquals(expected, snapshot.totalFrames)
        assertEquals(expected, snapshot.jankFrames)
        assertEquals(expected, snapshot.frozenFrames)
    }

    private fun recordFrames(
        total: Int,
        jank: Int = 0,
        frozen: Int = 0,
    ) {
        repeat(frozen) { stats.record(isJank = true, frameDurationNanos = frozenFrame) }
        repeat(jank - frozen) { stats.record(isJank = true, frameDurationNanos = jankFrame) }
        repeat(total - jank) { stats.record(isJank = false, frameDurationNanos = smoothFrame) }
    }

    @Test
    fun `비율은_전체_프레임_대비_천분율로_계산한다`() {
        recordFrames(total = 1000, jank = 30, frozen = 6)

        val snapshot = stats.snapshot()
        assertEquals(30L, snapshot.jankRatePermille)
        assertEquals(6L, snapshot.frozenRatePermille)
    }

    /** 내림이면 작은 비율이 전부 0으로 눌려 회귀를 못 본다. */
    @Test
    fun `비율은_내림이_아니라_반올림한다`() {
        // 3/400 = 7.5‰ → 8‰
        recordFrames(total = 400, jank = 3)

        assertEquals(8L, stats.snapshot().jankRatePermille)
    }

    @Test
    fun `모든_프레임이_jank면_1000천분율이다`() {
        recordFrames(total = 50, jank = 50)

        assertEquals(1000L, stats.snapshot().jankRatePermille)
    }

    @Test
    fun `프레임이_없으면_비율은_0이고_나눗셈이_터지지_않는다`() {
        val snapshot = stats.snapshot()

        assertEquals(0L, snapshot.jankRatePermille)
        assertEquals(0L, snapshot.frozenRatePermille)
    }

    @Test
    fun `분모가_최소_프레임_수에_미치지_못하면_비율을_신뢰하지_않는다`() {
        recordFrames(total = (ScreenFrameStats.MIN_FRAMES_FOR_RATE - 1).toInt(), jank = 1)

        assertFalse(stats.snapshot().isRateReliable)
    }

    @Test
    fun `분모가_최소_프레임_수에_도달하면_비율을_신뢰한다`() {
        recordFrames(total = ScreenFrameStats.MIN_FRAMES_FOR_RATE.toInt(), jank = 1)

        assertTrue(stats.snapshot().isRateReliable)
    }

    @Test
    fun `reset하면_비율_신뢰_여부도_되돌아간다`() {
        recordFrames(total = 100, jank = 10)
        assertTrue(stats.snapshot().isRateReliable)

        stats.reset()

        assertFalse(stats.snapshot().isRateReliable)
    }

    @Test
    fun `스냅샷은_이후의_기록에_영향받지_않는다`() {
        stats.record(isJank = true, frameDurationNanos = jankFrame)
        val snapshot = stats.snapshot()

        stats.record(isJank = true, frameDurationNanos = jankFrame)

        assertEquals(1L, snapshot.totalFrames)
        assertEquals(2L, stats.snapshot().totalFrames)
    }
}
