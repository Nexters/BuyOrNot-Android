package com.sseotdabwa.buyornot.core.analytics

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MixpanelEventMapperTest {
    @Test
    fun `푸시_알림_탭은_push_opened로_매핑된다`() {
        val (name, _) =
            AnalyticsEvent
                .PushOpened(
                    pushType = "MY_FEED_VOTED_10",
                    feedId = 123L,
                    notificationId = 42L,
                ).toMixpanelEvent()

        assertEquals("push_opened", name)
    }

    @Test
    fun `푸시_알림_탭의_속성이_모두_매핑된다`() {
        val (_, props) =
            AnalyticsEvent
                .PushOpened(
                    pushType = "MY_FEED_VOTED_10",
                    feedId = 123L,
                    notificationId = 42L,
                ).toMixpanelEvent()

        assertEquals("MY_FEED_VOTED_10", props.getString("push_type"))
        assertEquals(123L, props.getLong("feed_id"))
        assertEquals(42L, props.getLong("notification_id"))
    }

    @Test
    fun `마케팅_알림은_feed_id와_notification_id_속성이_생략된다`() {
        val (_, props) =
            AnalyticsEvent
                .PushOpened(
                    pushType = "MARKETING_NO_VOTE",
                    feedId = null,
                    notificationId = null,
                ).toMixpanelEvent()

        assertEquals("MARKETING_NO_VOTE", props.getString("push_type"))
        assertFalse(props.has("feed_id"))
        assertFalse(props.has("notification_id"))
    }

    @Test
    fun `feedId만_있으면_notification_id_속성만_생략된다`() {
        val (_, props) =
            AnalyticsEvent
                .PushOpened(
                    pushType = "MY_FEED_CLOSED",
                    feedId = 123L,
                    notificationId = null,
                ).toMixpanelEvent()

        assertTrue(props.has("feed_id"))
        assertFalse(props.has("notification_id"))
    }
}
