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
    fun `AI_초안_결과와_작성_방식이_snake_case로_매핑되고_없는_값은_생략된다`() {
        val (draftName, draftProps) =
            AnalyticsEvent
                .AiDraftCompleted(
                    result = AnalyticsEvent.AiDraftCompleted.Result.FAILED,
                    imageCount = 3,
                    durationMs = 12_000,
                    filledFields = null,
                ).toMixpanelEvent()
        assertEquals("ai_draft_completed", draftName)
        assertEquals("failed", draftProps.getString("result"))
        assertEquals(3, draftProps.getInt("image_count"))
        assertEquals(12_000L, draftProps.getLong("duration_ms"))
        assertFalse(draftProps.has("filled_fields"))

        val (completedName, completedProps) =
            AnalyticsEvent
                .VoteCreateCompleted(
                    itemId = 1,
                    voteTitle = "제목",
                    optionCount = 2,
                    writingMode = AnalyticsEvent.WritingMode.AI,
                    aiEditedFields = listOf("title"),
                ).toMixpanelEvent()
        assertEquals("vote_create_completed", completedName)
        assertEquals("ai", completedProps.getString("writing_mode"))
        assertEquals("title", completedProps.getJSONArray("ai_edited_fields").getString(0))

        val (abandonedName, abandonedProps) =
            AnalyticsEvent
                .VoteCreateAbandoned(
                    filledFields = listOf("images"),
                    lastStep = null,
                    step = AnalyticsEvent.VoteCreateAbandoned.Step.ALBUM,
                    writingMode = null,
                ).toMixpanelEvent()
        assertEquals("vote_create_abandoned", abandonedName)
        assertEquals("album", abandonedProps.getString("step"))
        assertFalse(abandonedProps.has("writing_mode"))

        val (confirmedName, confirmedProps) =
            AnalyticsEvent.VoteCreatePhotosConfirmed(imageCount = 2, useAi = true).toMixpanelEvent()
        assertEquals("vote_create_photos_confirmed", confirmedName)
        assertTrue(confirmedProps.getBoolean("use_ai"))
    }
}
