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

    @Test
    fun `AI_초안_생성_성공은_처리_경로와_함께_매핑된다`() {
        val (name, props) = AnalyticsEvent.AiDraftGenerated(source = "cloud", durationMs = 1200L).toMixpanelEvent()

        assertEquals("ai_draft_generated", name)
        assertEquals("cloud", props.getString("source"))
        assertEquals(1200L, props.getLong("duration_ms"))
    }

    @Test
    fun `AI_초안_게시는_수정_여부와_함께_매핑된다`() {
        val (name, props) =
            AnalyticsEvent
                .AiDraftSubmitted(source = "on_device", isTitleEdited = true, isContentEdited = false)
                .toMixpanelEvent()

        assertEquals("ai_draft_submitted", name)
        assertTrue(props.getBoolean("is_title_edited"))
        assertFalse(props.getBoolean("is_content_edited"))
    }
}
