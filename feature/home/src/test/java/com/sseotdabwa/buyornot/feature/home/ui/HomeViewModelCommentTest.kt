package com.sseotdabwa.buyornot.feature.home.ui

import com.sseotdabwa.buyornot.core.designsystem.components.FeedCommentPreview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelCommentTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val commentRepository = FakeCommentRepository()

    private fun createViewModel(feedRepository: FakeFeedRepository) =
        HomeViewModel(
            userPreferencesRepository = FakeUserPreferencesRepository(),
            feedRepository = feedRepository,
            commentRepository = commentRepository,
            userRepository = FakeUserRepository(),
            notificationRepository = FakeNotificationRepository(),
            analytics = FakeAnalytics(),
            performance = FakePerformance(),
        )

    @Test
    fun `피드의_댓글_수와_최신_댓글을_카드_미리보기로_옮긴다`() {
        val viewModel =
            createViewModel(
                FakeFeedRepository(
                    listOf(
                        testFeed(feedId = 1, commentCount = 12, latestComment = testCommentPreview("겁나 무거워요")),
                        testFeed(feedId = 2),
                    ),
                ),
            )

        val feeds = viewModel.uiState.value.feeds
        assertEquals(12, feeds[0].commentCount)
        assertEquals(FeedCommentPreview(nickname = "토봉이날다12456", content = "겁나 무거워요"), feeds[0].latestComment)
        assertNull(feeds[1].latestComment)
    }

    @Test
    fun `투표에_성공하면_응답의_대표_이미지로_투표_완료_스낵바를_띄운다`() =
        runTest {
            val feedRepository = FakeFeedRepository(listOf(testFeed(feedId = 1)))
            feedRepository.voteImageUrl = "https://cdn/thumbnail.jpg"
            val viewModel = createViewModel(feedRepository)

            viewModel.handleIntent(HomeIntent.OnVoteClicked(feedId = "1", optionIndex = 0))

            assertEquals(
                HomeSideEffect.ShowVoteCompletedSnackbar(feedId = 1, imageUrl = "https://cdn/thumbnail.jpg"),
                viewModel.sideEffect.first(),
            )
        }

    @Test
    fun `투표_응답에_대표_이미지가_없으면_첫_번째_상품_이미지를_쓴다`() =
        runTest {
            val viewModel = createViewModel(FakeFeedRepository(listOf(testFeed(feedId = 1))))

            viewModel.handleIntent(HomeIntent.OnVoteClicked(feedId = "1", optionIndex = 1))

            assertEquals(
                HomeSideEffect.ShowVoteCompletedSnackbar(feedId = 1, imageUrl = "https://cdn/1.jpg"),
                viewModel.sideEffect.first(),
            )
        }

    @Test
    fun `댓글_진입점을_누르면_입력창_포커스_없이_댓글_영역으로_이동한다`() =
        runTest {
            val viewModel = createViewModel(FakeFeedRepository(listOf(testFeed(feedId = 1))))

            viewModel.handleIntent(HomeIntent.OnCommentClicked(feedId = "1"))

            assertEquals(
                HomeSideEffect.NavigateToFeedComments(feedId = 1, focusCommentInput = false),
                viewModel.sideEffect.first(),
            )
        }

    @Test
    fun `다른_화면에서_댓글이_바뀌면_그_피드의_댓글_수와_미리보기만_다시_받는다`() =
        runTest {
            val feedRepository = FakeFeedRepository(listOf(testFeed(feedId = 1), testFeed(feedId = 2, commentCount = 5)))
            val viewModel = createViewModel(feedRepository)
            feedRepository.feeds =
                listOf(
                    testFeed(feedId = 1, commentCount = 1, latestComment = testCommentPreview("방금 쓴 댓글")),
                    testFeed(feedId = 2, commentCount = 99),
                )

            commentRepository.commentChanges.emit(1L)

            val feeds = viewModel.uiState.value.feeds
            assertEquals(1, feeds[0].commentCount)
            assertEquals("방금 쓴 댓글", feeds[0].latestComment?.content)
            // 신호가 없던 피드는 그대로 둔다.
            assertEquals(5, feeds[1].commentCount)
        }
}
