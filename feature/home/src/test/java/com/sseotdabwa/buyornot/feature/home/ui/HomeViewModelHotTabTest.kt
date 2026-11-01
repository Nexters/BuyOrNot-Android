package com.sseotdabwa.buyornot.feature.home.ui

import com.sseotdabwa.buyornot.domain.model.FeedCategory
import com.sseotdabwa.buyornot.domain.repository.HotFeedList
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeViewModelHotTabTest {
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
    fun `핫플_투표_탭을_고르면_핫플_목록을_불러온다`() {
        val feedRepository = FakeFeedRepository(listOf(testFeed(feedId = 1)))
        feedRepository.hotPages =
            mapOf(null to HotFeedList(feeds = listOf(testFeed(feedId = 7), testFeed(feedId = 8)), nextCursor = null, hasNext = false))
        val viewModel = createViewModel(feedRepository)

        viewModel.handleIntent(HomeIntent.OnTabSelected(HomeTab.HOT))

        val state = viewModel.uiState.value
        assertEquals(HomeTab.HOT, state.selectedTab)
        assertEquals(listOf("7", "8"), state.feeds.map { it.id })
        assertEquals(HotFeedListRequest(cursor = null, category = null), feedRepository.hotRequests.last())
    }

    @Test
    fun `핫플_투표_다음_페이지는_문자열_커서로_불러오고_이미_받은_피드는_걷어낸다`() {
        val feedRepository = FakeFeedRepository(emptyList())
        feedRepository.hotPages =
            mapOf(
                null to HotFeedList(feeds = listOf(testFeed(feedId = 1), testFeed(feedId = 2)), nextCursor = "10_2", hasNext = true),
                "10_2" to HotFeedList(feeds = listOf(testFeed(feedId = 2), testFeed(feedId = 3)), nextCursor = null, hasNext = false),
            )
        val viewModel = createViewModel(feedRepository)
        viewModel.handleIntent(HomeIntent.OnTabSelected(HomeTab.HOT))

        viewModel.handleIntent(HomeIntent.LoadNextPage)

        val state = viewModel.uiState.value
        assertEquals("10_2", feedRepository.hotRequests.last().cursor)
        assertEquals(listOf("1", "2", "3"), state.feeds.map { it.id })
        assertFalse(state.hasNextPage)
    }

    @Test
    fun `핫플_투표는_고른_카테고리를_서버에_넘긴다`() {
        val feedRepository = FakeFeedRepository(emptyList())
        val viewModel = createViewModel(feedRepository)
        viewModel.handleIntent(HomeIntent.OnTabSelected(HomeTab.HOT))

        viewModel.handleIntent(HomeIntent.OnCategoryToggled(FeedCategory.FOOD))

        assertEquals(HotFeedListRequest(cursor = null, category = listOf("FOOD")), feedRepository.hotRequests.last())
    }

    @Test
    fun `핫플_투표_탭에서는_투표_상태_필터를_열거나_적용하지_않는다`() {
        val feedRepository = FakeFeedRepository(emptyList())
        val viewModel = createViewModel(feedRepository)
        viewModel.handleIntent(HomeIntent.OnTabSelected(HomeTab.HOT))
        val hotRequestCount = feedRepository.hotRequests.size
        val feedListRequestCount = feedRepository.feedListRequests.size

        viewModel.handleIntent(HomeIntent.ShowSortSheet)
        viewModel.handleIntent(HomeIntent.OnFilterSelected(FilterChip.ENDED))

        val state = viewModel.uiState.value
        assertFalse(state.showSortSheet)
        assertEquals(FilterChip.ALL, state.selectedFilter)
        assertEquals(hotRequestCount, feedRepository.hotRequests.size)
        assertEquals(feedListRequestCount, feedRepository.feedListRequests.size)
    }

    @Test
    fun `투표_피드의_다음_페이지는_숫자_커서로_불러온다`() {
        val feedRepository = FakeFeedRepository(listOf(testFeed(feedId = 5)))
        feedRepository.feedListNextCursor = 5L
        val viewModel = createViewModel(feedRepository)
        assertTrue(viewModel.uiState.value.hasNextPage)

        viewModel.handleIntent(HomeIntent.LoadNextPage)

        assertEquals(5L, feedRepository.feedListRequests.last().cursor)
    }

    @Test
    fun `이미_적용한_초기_탭_요청이_다시_오면_사용자가_고른_탭을_유지한다`() {
        val feedRepository = FakeFeedRepository(listOf(testFeed(feedId = 1)))
        feedRepository.hotPages = mapOf(null to HotFeedList(feeds = listOf(testFeed(feedId = 7)), nextCursor = null, hasNext = false))
        val viewModel = createViewModel(feedRepository)
        viewModel.handleIntent(HomeIntent.ApplyInitialTab(tab = HomeTab.FEED, requestId = 0L))
        viewModel.handleIntent(HomeIntent.OnTabSelected(HomeTab.HOT))

        viewModel.handleIntent(HomeIntent.ApplyInitialTab(tab = HomeTab.FEED, requestId = 0L))

        assertEquals(HomeTab.HOT, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun `새_초기_탭_요청은_사용자가_고른_탭보다_우선한다`() {
        val feedRepository = FakeFeedRepository(listOf(testFeed(feedId = 1)))
        feedRepository.hotPages = mapOf(null to HotFeedList(feeds = listOf(testFeed(feedId = 7)), nextCursor = null, hasNext = false))
        val viewModel = createViewModel(feedRepository)
        viewModel.handleIntent(HomeIntent.ApplyInitialTab(tab = HomeTab.FEED, requestId = 0L))
        viewModel.handleIntent(HomeIntent.OnTabSelected(HomeTab.HOT))

        viewModel.handleIntent(HomeIntent.ApplyInitialTab(tab = HomeTab.FEED, requestId = 1L))

        assertEquals(HomeTab.FEED, viewModel.uiState.value.selectedTab)
    }
}
