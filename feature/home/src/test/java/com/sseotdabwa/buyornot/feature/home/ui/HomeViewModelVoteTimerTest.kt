package com.sseotdabwa.buyornot.feature.home.ui

import org.junit.Rule
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HomeViewModelVoteTimerTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(feedRepository: FakeFeedRepository) =
        HomeViewModel(
            userPreferencesRepository = FakeUserPreferencesRepository(),
            feedRepository = feedRepository,
            commentRepository = FakeCommentRepository(),
            userRepository = FakeUserRepository(),
            notificationRepository = FakeNotificationRepository(),
            analytics = FakeAnalytics(),
            performance = FakePerformance(),
            createNudgeSession = FeedCreateNudgeSession(),
        )

    @Test
    fun `오프셋_없는_마감_시각을_한국_시간으로_읽어_카드에_옮긴다`() {
        val viewModel =
            createViewModel(FakeFeedRepository(listOf(testFeed(feedId = 1, voteClosedAt = "2026-10-30T17:13:45"))))

        assertEquals(
            Instant.parse("2026-10-30T08:13:45Z").toEpochMilli(),
            viewModel.uiState.value.feeds
                .single()
                .voteClosedAtMillis,
        )
    }

    @Test
    fun `마감_시각이_없으면_타이머용_시각도_비운다`() {
        val viewModel = createViewModel(FakeFeedRepository(listOf(testFeed(feedId = 1))))

        assertNull(
            viewModel.uiState.value.feeds
                .single()
                .voteClosedAtMillis,
        )
    }

    @Test
    fun `마감_시각이_지난_피드는_서버가_진행중이라_해도_투표하지_않는다`() {
        val feedRepository = FakeFeedRepository(listOf(testFeed(feedId = 1, voteClosedAt = "2020-01-01T00:00:00")))
        val viewModel = createViewModel(feedRepository)

        viewModel.handleIntent(HomeIntent.OnVoteClicked(feedId = "1", optionIndex = 0))

        assertEquals(0, feedRepository.voteRequestCount)
        assertNull(
            viewModel.uiState.value.feeds
                .single()
                .userVotedOptionIndex,
        )
    }

    @Test
    fun `마감_시각이_남은_피드에는_투표한다`() {
        val feedRepository = FakeFeedRepository(listOf(testFeed(feedId = 1, voteClosedAt = "2099-01-01T00:00:00")))
        val viewModel = createViewModel(feedRepository)

        viewModel.handleIntent(HomeIntent.OnVoteClicked(feedId = "1", optionIndex = 0))

        assertEquals(1, feedRepository.voteRequestCount)
        assertEquals(
            0,
            viewModel.uiState.value.feeds
                .single()
                .userVotedOptionIndex,
        )
    }

    @Test
    fun `마감_시각에_닿는_순간부터_마감으로_본다`() {
        val feed = testFeedItem(voteClosedAtMillis = 1_000L)

        assertFalse(feed.isVoteEndedAt(nowMillis = 999L))
        assertTrue(feed.isVoteEndedAt(nowMillis = 1_000L))
    }

    @Test
    fun `마감_시각이_없으면_서버_상태로만_마감을_판단한다`() {
        assertFalse(testFeedItem(voteClosedAtMillis = null).isVoteEndedAt(nowMillis = Long.MAX_VALUE))
        assertTrue(testFeedItem(voteClosedAtMillis = null, isVoteEnded = true).isVoteEndedAt(nowMillis = 0L))
    }

    private fun testFeedItem(
        voteClosedAtMillis: Long?,
        isVoteEnded: Boolean = false,
    ) = FeedItem(
        id = "1",
        profileImageUrl = "",
        nickname = "참새방앗간12456",
        category = "기타",
        createdAt = "6시간 전",
        title = "",
        content = "",
        productImageUrls = emptyList(),
        price = "31,900",
        imageAspectRatios = emptyList(),
        isVoteEnded = isVoteEnded,
        userVotedOptionIndex = null,
        buyVoteCount = 0,
        maybeVoteCount = 0,
        totalVoteCount = 0,
        isOwner = false,
        authorUserId = 2L,
        voteClosedAtMillis = voteClosedAtMillis,
    )
}
