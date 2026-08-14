package com.sseotdabwa.buyornot.core.analytics.performance

import org.junit.Test
import kotlin.test.assertEquals

class SingleShotPerfTraceTest {
    private class RecordingPerfTrace : PerfTrace {
        val calls = mutableListOf<String>()

        override fun start() {
            calls += "start"
        }

        override fun stop() {
            calls += "stop"
        }

        override fun putAttribute(
            name: String,
            value: String,
        ) {
            calls += "attribute:$name=$value"
        }

        override fun putMetric(
            name: String,
            value: Long,
        ) {
            calls += "metric:$name=$value"
        }
    }

    @Test
    fun `start와_stop은_delegate로_한_번씩_전달된다`() {
        val delegate = RecordingPerfTrace()
        val trace = SingleShotPerfTrace(delegate)

        trace.start()
        trace.stop()

        assertEquals(listOf("start", "stop"), delegate.calls)
    }

    @Test
    fun `중복_start는_무시된다`() {
        val delegate = RecordingPerfTrace()
        val trace = SingleShotPerfTrace(delegate)

        trace.start()
        trace.start()
        trace.start()

        assertEquals(listOf("start"), delegate.calls)
    }

    @Test
    fun `start_없이_호출된_stop은_무시된다`() {
        val delegate = RecordingPerfTrace()
        val trace = SingleShotPerfTrace(delegate)

        trace.stop()

        assertEquals(emptyList<String>(), delegate.calls)
    }

    @Test
    fun `중복_stop은_무시된다`() {
        val delegate = RecordingPerfTrace()
        val trace = SingleShotPerfTrace(delegate)

        trace.start()
        trace.stop()
        trace.stop()

        assertEquals(listOf("start", "stop"), delegate.calls)
    }

    @Test
    fun `종료_전_속성과_측정항목은_전달된다`() {
        val delegate = RecordingPerfTrace()
        val trace = SingleShotPerfTrace(delegate)

        trace.start()
        trace.putAttribute("result", "success")
        trace.putMetric("feed_count", 20L)
        trace.stop()

        assertEquals(
            listOf("start", "attribute:result=success", "metric:feed_count=20", "stop"),
            delegate.calls,
        )
    }

    @Test
    fun `종료_후_속성과_측정항목_추가는_무시된다`() {
        val delegate = RecordingPerfTrace()
        val trace = SingleShotPerfTrace(delegate)

        trace.start()
        trace.stop()
        trace.putAttribute("result", "success")
        trace.putMetric("feed_count", 20L)

        assertEquals(listOf("start", "stop"), delegate.calls)
    }
}
