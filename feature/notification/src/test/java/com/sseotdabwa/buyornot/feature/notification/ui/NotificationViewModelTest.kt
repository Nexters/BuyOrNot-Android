package com.sseotdabwa.buyornot.feature.notification.ui

import com.sseotdabwa.buyornot.domain.model.ImageSource
import com.sseotdabwa.buyornot.domain.model.Notification
import com.sseotdabwa.buyornot.domain.model.NotificationFilter
import com.sseotdabwa.buyornot.domain.model.NotificationType
import com.sseotdabwa.buyornot.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class NotificationViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(vararg notifications: Notification) =
        NotificationViewModel(
            appPreferencesRepository = FakeAppPreferencesRepository(),
            notificationRepository = FakeNotificationRepository(notifications.toList()),
        )

    @Test
    fun `칩을_고르면_받아둔_알림을_그_칩의_유형으로_거른다`() {
        val viewModel =
            createViewModel(
                testNotification(id = 1, type = NotificationType.MY_FEED_CLOSED),
                testNotification(id = 2, type = NotificationType.MY_FEED_VOTED_10),
                testNotification(id = 3, type = NotificationType.PARTICIPATED_FEED_CLOSED),
                testNotification(id = 4, type = NotificationType.COMMENT_REPLIED),
                testNotification(id = 5, type = NotificationType.MARKETING_NO_VOTE),
            )

        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), viewModel.visibleIds())
        viewModel.handleIntent(NotificationIntent.OnFilterSelected(NotificationFilter.MY_VOTE))
        assertEquals(listOf(1L, 2L), viewModel.visibleIds())
        viewModel.handleIntent(NotificationIntent.OnFilterSelected(NotificationFilter.PARTICIPATED))
        assertEquals(listOf(3L), viewModel.visibleIds())
        viewModel.handleIntent(NotificationIntent.OnFilterSelected(NotificationFilter.COMMENT))
        assertEquals(listOf(4L), viewModel.visibleIds())
    }

    @Test
    fun `안_읽은_알림이_있는_칩에만_점을_찍고_읽으면_지운다`() {
        val viewModel =
            createViewModel(
                testNotification(id = 1, type = NotificationType.MY_FEED_CLOSED, isRead = true),
                testNotification(id = 2, type = NotificationType.COMMENT_REPLIED, isRead = false),
                testNotification(id = 3, type = NotificationType.MARKETING_NO_VOTE, isRead = false),
            )

        assertEquals(setOf(NotificationFilter.COMMENT), viewModel.uiState.value.unreadFilters)

        viewModel.handleIntent(NotificationIntent.OnNotificationClick(viewModel.uiState.value.notifications[1]))

        assertEquals(emptySet(), viewModel.uiState.value.unreadFilters)
    }

    @Test
    fun `투표_마감은_이긴_선택지를_답글은_답글_내용을_아랫줄에_보여준다`() {
        val viewModel =
            createViewModel(
                testNotification(id = 1, type = NotificationType.MY_FEED_CLOSED, resultLabel = "NO"),
                testNotification(id = 2, type = NotificationType.PARTICIPATED_FEED_CLOSED, resultLabel = "TIE", body = "동률이에요"),
                testNotification(id = 3, type = NotificationType.COMMENT_REPLIED, body = "그래서 결국 사셨나요?"),
            )

        val items = viewModel.uiState.value.notifications
        assertEquals(listOf("‘애매하긴 해..’ 승!", "동률이에요", "‘그래서 결국 사셨나요?’"), items.map { it.message })
        assertEquals("록시땅 바디로션", items[0].headline)
        assertEquals(null, items[2].time)
    }

    @Test
    fun `답글_알림을_누르면_원댓글_id와_함께_상세로_이동한다`() =
        runTest {
            val viewModel =
                createViewModel(testNotification(id = 4, type = NotificationType.COMMENT_REPLIED, commentId = 30L))

            viewModel.handleIntent(
                NotificationIntent.OnNotificationClick(
                    viewModel.uiState.value.notifications
                        .single(),
                ),
            )

            assertEquals(
                NotificationSideEffect.NavigateToNotificationDetail(notificationId = 4L, feedId = 100L, commentId = 30L),
                viewModel.sideEffect.first(),
            )
        }

    private fun NotificationViewModel.visibleIds() = uiState.value.notifications.map { it.id }
}

private fun testNotification(
    id: Long,
    type: NotificationType,
    isRead: Boolean = true,
    resultLabel: String = "YES",
    body: String = "",
    commentId: Long? = null,
) = Notification(
    notificationId = id,
    feedId = 100L,
    type = type,
    title = "알림 제목",
    body = body,
    isRead = isRead,
    voteClosedAt = "2026-11-14T12:00:00",
    resultPercent = 60,
    resultLabel = resultLabel,
    feedImage = ImageSource("https://example.com/feed.jpg"),
    feedTitle = "록시땅 바디로션",
    commentId = commentId,
)

private class FakeAppPreferencesRepository : AppPreferencesRepository {
    override val hasRequestedNotificationPermission: Flow<Boolean> = flowOf(false)
    override val isFirstRun: Flow<Boolean> = flowOf(false)
    override val lastSoftUpdateShownTime: Flow<Long> = flowOf(0L)

    override suspend fun updateNotificationPermissionRequested(requested: Boolean) = Unit

    override suspend fun updateIsFirstRun(isFirstRun: Boolean) = Unit

    override suspend fun updateLastSoftUpdateShownTime(timeMillis: Long) = Unit
}
