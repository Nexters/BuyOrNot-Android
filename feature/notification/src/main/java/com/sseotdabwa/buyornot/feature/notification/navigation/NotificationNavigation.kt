package com.sseotdabwa.buyornot.feature.notification.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sseotdabwa.buyornot.feature.notification.ui.NotificationDetailScreen
import com.sseotdabwa.buyornot.feature.notification.ui.NotificationScreen

const val NOTIFICATION_ROUTE = "notification"
const val NOTIFICATION_DETAIL_ROUTE = "notification_detail"

fun NavGraphBuilder.notificationGraph(
    onBackClick: () -> Unit,
    onNotificationClick: (String) -> Unit,
) {
    composable(route = NOTIFICATION_ROUTE) {
        NotificationScreen(
            onBackClick = onBackClick,
            onNotificationClick = onNotificationClick,
        )
    }

    composable(
        route = "$NOTIFICATION_DETAIL_ROUTE/{notificationId}",
        arguments =
            listOf(
                navArgument("notificationId") { type = NavType.StringType },
            ),
    ) { backStackEntry ->
        val notificationId = backStackEntry.arguments?.getString("notificationId") ?: ""
        NotificationDetailScreen(
            notificationId = notificationId,
            onBackClick = onBackClick,
        )
    }
}

fun NavHostController.navigateToNotification(navOptions: NavOptions? = null) {
    this.navigate(NOTIFICATION_ROUTE, navOptions)
}

// 상세 화면으로 이동하는 함수
fun NavHostController.navigateToNotificationDetail(notificationId: String) {
    this.navigate("$NOTIFICATION_DETAIL_ROUTE/$notificationId")
}
