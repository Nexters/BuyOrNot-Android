package com.sseotdabwa.buyornot.feature.home.ui

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelCreateNudgeTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val session = FeedCreateNudgeSession()

    private fun createViewModel() =
        HomeViewModel(
            userPreferencesRepository = FakeUserPreferencesRepository(),
            feedRepository = FakeFeedRepository(listOf(testFeed(feedId = 1), testFeed(feedId = 2), testFeed(feedId = 3))),
            commentRepository = FakeCommentRepository(),
            userRepository = FakeUserRepository(),
            notificationRepository = FakeNotificationRepository(),
            analytics = FakeAnalytics(),
            performance = FakePerformance(),
            createNudgeSession = session,
        )

    @Test
    fun `투표_피드에서_2행_경계에_도달하면_넛지를_띄운다`() {
        val viewModel = createViewModel()

        viewModel.handleIntent(HomeIntent.OnCreateNudgeAnchorReached)

        assertTrue(viewModel.uiState.value.showCreateNudge)
    }

    @Test
    fun `넛지를_닫은_뒤_다시_경계에_도달해도_띄우지_않는다`() {
        val viewModel = createViewModel()
        viewModel.handleIntent(HomeIntent.OnCreateNudgeAnchorReached)
        viewModel.handleIntent(HomeIntent.DismissCreateNudge)

        viewModel.handleIntent(HomeIntent.OnCreateNudgeAnchorReached)

        assertFalse(viewModel.uiState.value.showCreateNudge)
    }

    @Test
    fun `같은_세션이면_홈을_새로_만들어도_넛지를_다시_띄우지_않는다`() {
        createViewModel().handleIntent(HomeIntent.OnCreateNudgeAnchorReached)
        val newViewModel = createViewModel()

        newViewModel.handleIntent(HomeIntent.OnCreateNudgeAnchorReached)

        assertFalse(newViewModel.uiState.value.showCreateNudge)
    }

    @Test
    fun `내_투표_탭에서는_넛지를_띄우지_않고_세션에도_남기지_않는다`() {
        val viewModel = createViewModel()
        viewModel.handleIntent(HomeIntent.OnTabSelected(HomeTab.MY_FEED))

        viewModel.handleIntent(HomeIntent.OnCreateNudgeAnchorReached)

        assertFalse(viewModel.uiState.value.showCreateNudge)
        assertFalse(session.hasShown)
    }

    @Test
    fun `투표_등록을_누르면_넛지를_닫고_등록_화면으로_이동한다`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.handleIntent(HomeIntent.OnCreateNudgeAnchorReached)

            viewModel.handleIntent(HomeIntent.OnCreateNudgeRegisterClicked)

            assertFalse(viewModel.uiState.value.showCreateNudge)
            assertEquals(HomeSideEffect.NavigateToUpload, viewModel.sideEffect.first())
        }
}
