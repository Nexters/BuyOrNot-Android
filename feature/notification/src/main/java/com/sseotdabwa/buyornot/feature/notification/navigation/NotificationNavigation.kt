package com.sseotdabwa.buyornot.feature.notification.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import com.sseotdabwa.buyornot.feature.notification.ui.NotificationDetailRoute as NotificationDetailScreen
import com.sseotdabwa.buyornot.feature.notification.ui.NotificationRoute as NotificationScreen

@Serializable
data object NotificationRoute

/**
 * 피드 상세. 알림·딥링크·홈 댓글 진입점이 함께 쓴다.
 *
 * 인자 이름은 [com.sseotdabwa.buyornot.feature.notification.ui.NotificationDetailViewModel]이
 * SavedStateHandle에서 같은 이름으로 읽는다.
 *
 * @property scrollToComments 댓글 진입점에서 들어왔으면 댓글 영역으로 스크롤한다.
 * @property focusCommentInput [의견 남기기]로 들어왔으면 댓글 입력창에 포커스한다.
 */
@Serializable
data class NotificationDetailRoute(
    val feedId: Long,
    val notificationId: Long = -1L,
    val scrollToComments: Boolean = false,
    val focusCommentInput: Boolean = false,
)

fun NavGraphBuilder.notificationGraph(
    onBackClick: () -> Unit,
    onNotificationClick: (Long, Long) -> Unit,
    onLinkClick: (url: String) -> Unit = {},
    onShareClick: (feedId: Long, title: String) -> Unit = { _, _ -> },
    onImageClick: (imageUrls: List<String>, page: Int) -> Unit = { _, _ -> },
) {
    composable<NotificationRoute> {
        NotificationScreen(
            onBackClick = onBackClick,
            onNotificationClick = onNotificationClick,
        )
    }

    composable<NotificationDetailRoute> {
        NotificationDetailScreen(
            onBackClick = onBackClick,
            onLinkClick = onLinkClick,
            onShareClick = onShareClick,
            onImageClick = onImageClick,
        )
    }
}

fun NavHostController.navigateToNotification(navOptions: NavOptions? = null) {
    navigate(NotificationRoute, navOptions)
}

fun NavHostController.navigateToNotificationDetail(
    notificationId: Long = -1L,
    feedId: Long,
) {
    navigate(NotificationDetailRoute(feedId = feedId, notificationId = notificationId))
}

/**
 * 홈 피드 카드의 댓글 진입점. [focusCommentInput]이면 [의견 남기기]처럼 입력창까지 포커스한다.
 */
fun NavHostController.navigateToFeedComments(
    feedId: Long,
    focusCommentInput: Boolean = false,
) {
    navigate(
        NotificationDetailRoute(
            feedId = feedId,
            scrollToComments = true,
            focusCommentInput = focusCommentInput,
        ),
    )
}

/**
 * FCM 딥링크 진입점. notificationId가 있으면 함께 넘겨 markAsRead까지 수행한다.
 */
fun NavHostController.navigateToFeedDetail(
    feedId: Long,
    notificationId: Long? = null,
) {
    navigate(NotificationDetailRoute(feedId = feedId, notificationId = notificationId ?: -1L))
}
