package com.sseotdabwa.buyornot.core.analytics

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdentityBufferingAnalyticsTest {
    private lateinit var fake: FakeAnalytics
    private lateinit var sut: IdentityBufferingAnalytics

    @Before
    fun setUp() {
        fake = FakeAnalytics()
        sut = IdentityBufferingAnalytics(fake)
    }

    @Test
    fun `identify_전에_발행된_이벤트는_delegate로_전달되지_않는다`() {
        sut.track(AnalyticsEvent.PushOpened(pushType = "TYPE_A", feedId = 1L, notificationId = 10L))
        sut.track(AnalyticsEvent.FeedViewed(firstVisibleItemIndex = 0))

        assertTrue(fake.trackCalls.isEmpty())
    }

    @Test
    fun `identify_직후_보류됐던_이벤트가_원래_순서대로_전달된다`() {
        val event1 = AnalyticsEvent.PushOpened(pushType = "TYPE_A", feedId = 1L, notificationId = 10L)
        val event2 = AnalyticsEvent.FeedViewed(firstVisibleItemIndex = 0)
        val event3 = AnalyticsEvent.FeedViewed(firstVisibleItemIndex = 5)
        sut.track(event1)
        sut.track(event2)
        sut.track(event3)

        sut.identify("user-42")

        val trackedEvents = fake.trackCalls
        assertEquals(3, trackedEvents.size)
        assertEquals(event1, trackedEvents[0])
        assertEquals(event2, trackedEvents[1])
        assertEquals(event3, trackedEvents[2])
    }

    @Test
    fun `identify가_delegate로_전달된_뒤에_보류_이벤트가_flush된다`() {
        sut.track(AnalyticsEvent.PushOpened(pushType = "TYPE_A", feedId = 1L, notificationId = 10L))

        sut.identify("user-42")

        val calls = fake.allCalls
        val identifyIndex = calls.indexOfFirst { it is FakeAnalytics.Call.Identify }
        val firstTrackIndex = calls.indexOfFirst { it is FakeAnalytics.Call.Track }
        assertTrue(identifyIndex >= 0)
        assertTrue(firstTrackIndex > identifyIndex)
    }

    @Test
    fun `identify_이후_발행된_이벤트는_버퍼링_없이_바로_전달된다`() {
        sut.identify("user-42")

        val event = AnalyticsEvent.FeedViewed(firstVisibleItemIndex = 3)
        sut.track(event)

        assertEquals(listOf(event), fake.trackCalls)
    }

    @Test
    fun `identify가_두_번_호출돼도_이미_flush된_이벤트가_다시_전달되지_않는다`() {
        sut.track(AnalyticsEvent.PushOpened(pushType = "TYPE_A", feedId = 1L, notificationId = 10L))

        sut.identify("user-42")
        sut.identify("user-42")

        assertEquals(1, fake.trackCalls.size)
    }

    // -----------------------------------------------------------------------
    // Fake
    // -----------------------------------------------------------------------

    private class FakeAnalytics : Analytics {
        sealed class Call {
            data class Track(
                val event: AnalyticsEvent,
            ) : Call()

            data class Identify(
                val userId: String?,
            ) : Call()
        }

        val allCalls = mutableListOf<Call>()

        val trackCalls: List<AnalyticsEvent>
            get() = allCalls.filterIsInstance<Call.Track>().map { it.event }

        override fun track(event: AnalyticsEvent) {
            allCalls.add(Call.Track(event))
        }

        override fun identify(userId: String?) {
            allCalls.add(Call.Identify(userId))
        }
    }
}
