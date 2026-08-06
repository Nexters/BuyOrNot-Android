package com.sseotdabwa.buyornot.performance

import com.sseotdabwa.buyornot.core.analytics.performance.PerfTrace
import com.sseotdabwa.buyornot.core.analytics.performance.Performance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenPerformanceTrackerTest {
    private val performance = FakePerformance()
    private val tracker =
        ScreenPerformanceTracker(
            performance = performance,
            nonMeaningfulScreens = setOf(SPLASH),
        )

    private fun renderTracesOf(screen: String) = performance.tracesNamed("screen_render_$screen")

    private fun framesTracesOf(screen: String) = performance.tracesNamed("screen_frames_$screen")

    @Test
    fun `화면에_진입하면_render와_frames_Trace를_함께_시작한다`() {
        tracker.onScreenEntered(screen = HOME, sessionId = "entry-1")

        assertEquals(1, renderTracesOf(HOME).size)
        assertEquals(1, framesTracesOf(HOME).size)
        assertTrue(performance.traces.all { it.started })
    }

    /**
     * route는 인자가 채워지지 않은 패턴이라 피드 A 상세와 B 상세의 화면 이름이 같다.
     * 이름으로 구간을 가르면 B의 Trace가 아예 열리지 않는다.
     */
    @Test
    fun `이름이_같아도_back_stack_entry가_다르면_새_render_Trace를_연다`() {
        tracker.onScreenEntered(screen = DETAIL, sessionId = "entry-1")
        tracker.onScreenContentRendered()

        tracker.onScreenEntered(screen = DETAIL, sessionId = "entry-2")
        tracker.onScreenContentRendered()

        val renderTraces = renderTracesOf(DETAIL)
        assertEquals(2, renderTraces.size)
        assertTrue(renderTraces.all { it.stopped })
    }

    @Test
    fun `같은_back_stack_entry로_다시_호출하면_구간을_새로_열지_않는다`() {
        tracker.onScreenEntered(screen = HOME, sessionId = "entry-1")
        tracker.onScreenEntered(screen = HOME, sessionId = "entry-1")

        assertEquals(1, renderTracesOf(HOME).size)
        assertEquals(1, framesTracesOf(HOME).size)
    }

    @Test
    fun `이름이_같은_연속_진입의_프레임_집계는_섞이지_않는다`() {
        tracker.onScreenEntered(screen = DETAIL, sessionId = "entry-1")
        repeat(3) { tracker.onFrame(isJank = true, frameDurationNanos = JANK_FRAME) }

        tracker.onScreenEntered(screen = DETAIL, sessionId = "entry-2")
        tracker.onFrame(isJank = false, frameDurationNanos = SMOOTH_FRAME)
        tracker.onPaused()

        val framesTraces = framesTracesOf(DETAIL)
        assertEquals(2, framesTraces.size)
        assertEquals(3L, framesTraces[0].metrics["total_frames"])
        assertEquals(3L, framesTraces[0].metrics["jank_frames"])
        assertEquals(1L, framesTraces[1].metrics["total_frames"])
        assertEquals(0L, framesTraces[1].metrics["jank_frames"])
    }

    @Test
    fun `프레임이_한_장도_없는_구간은_보고하지_않는다`() {
        tracker.onScreenEntered(screen = HOME, sessionId = "entry-1")
        tracker.onScreenEntered(screen = DETAIL, sessionId = "entry-2")

        assertFalse(framesTracesOf(HOME).single().stopped)
    }

    @Test
    fun `콘텐츠_첫_프레임_전에_이탈하면_render_Trace를_버린다`() {
        tracker.onScreenEntered(screen = HOME, sessionId = "entry-1")
        tracker.onScreenEntered(screen = DETAIL, sessionId = "entry-2")

        assertFalse(renderTracesOf(HOME).single().stopped)
    }

    @Test
    fun `스플래시가_그려진_것만으로는_TTFD를_보고하지_않는다`() {
        tracker.onScreenEntered(screen = SPLASH, sessionId = "entry-1")
        tracker.onScreenContentRendered()

        assertFalse(tracker.isFirstMeaningfulRenderDone)
    }

    /** 로그인 시작 경로에서는 로그인 화면이 첫 의미 있는 화면이다. */
    @Test
    fun `로그인_화면이_그려지면_TTFD를_보고한다`() {
        tracker.onScreenEntered(screen = SPLASH, sessionId = "entry-1")
        tracker.onScreenContentRendered()

        tracker.onScreenEntered(screen = AUTH, sessionId = "entry-2")
        tracker.onScreenContentRendered()

        assertTrue(tracker.isFirstMeaningfulRenderDone)
    }

    private companion object {
        const val SPLASH = "splash"
        const val AUTH = "auth"
        const val HOME = "home"
        const val DETAIL = "notification_detail"

        const val SMOOTH_FRAME = 8_000_000L
        const val JANK_FRAME = 20_000_000L
    }
}

private class FakePerformance : Performance {
    val traces = mutableListOf<FakeTrace>()

    override fun newTrace(name: String): PerfTrace = FakeTrace(name).also { traces += it }

    fun tracesNamed(name: String): List<FakeTrace> = traces.filter { it.name == name }
}

private class FakeTrace(
    val name: String,
) : PerfTrace {
    var started = false
        private set

    var stopped = false
        private set

    val metrics = mutableMapOf<String, Long>()

    override fun start() {
        started = true
    }

    override fun stop() {
        stopped = true
    }

    override fun putAttribute(
        name: String,
        value: String,
    ) = Unit

    override fun putMetric(
        name: String,
        value: Long,
    ) {
        metrics[name] = value
    }
}
