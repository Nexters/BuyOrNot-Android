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

@Serializable
data class NotificationDetailRoute(
    val feedId: Long,
    val notificationId: Long = -1L,
)

fun NavGraphBuilder.notificationGraph(
    onBackClick: () -> Unit,
    onNotificationClick: (Long, Long) -> Unit,
    onLinkClick: (url: String) -> Unit = {},
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
 * FCM 딥링크 진입점. notificationId가 있으면 함께 넘겨 markAsRead까지 수행한다.
 */
fun NavHostController.navigateToFeedDetail(
    feedId: Long,
    notificationId: Long? = null,
) {
    navigate(NotificationDetailRoute(feedId = feedId, notificationId = notificationId ?: -1L))
}
