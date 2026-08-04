package com.sseotdabwa.buyornot.core.analytics.performance

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScreenFrameStatsTest {
    private val stats = ScreenFrameStats()

    private val smoothFrame = 8_000_000L
    private val jankFrame = 20_000_000L
    private val frozenFrame = ScreenFrameStats.FROZEN_FRAME_THRESHOLD_NANOS

    @Test
    fun `프레임이 없으면 비어 있다`() {
        assertTrue(stats.isEmpty)
    }

    @Test
    fun `전체 프레임과 jank 프레임을 따로 센다`() {
        stats.record(isJank = false, frameDurationNanos = smoothFrame)
        stats.record(isJank = true, frameDurationNanos = jankFrame)
        stats.record(isJank = true, frameDurationNanos = jankFrame)

        assertFalse(stats.isEmpty)
        assertEquals(3L, stats.totalFrames)
        assertEquals(2L, stats.jankFrames)
    }

    @Test
    fun `700ms 이상은 frozen 프레임으로도 센다`() {
        stats.record(isJank = true, frameDurationNanos = frozenFrame)

        assertEquals(1L, stats.jankFrames)
        assertEquals(1L, stats.frozenFrames)
    }

    @Test
    fun `700ms 미만은 frozen이 아니다`() {
        stats.record(isJank = true, frameDurationNanos = frozenFrame - 1)

        assertEquals(1L, stats.jankFrames)
        assertEquals(0L, stats.frozenFrames)
    }

    @Test
    fun `reset은 이전 화면의 집계를 남기지 않는다`() {
        stats.record(isJank = true, frameDurationNanos = frozenFrame)

        stats.reset()

        assertTrue(stats.isEmpty)
        assertEquals(0L, stats.totalFrames)
        assertEquals(0L, stats.jankFrames)
        assertEquals(0L, stats.frozenFrames)
    }
}
