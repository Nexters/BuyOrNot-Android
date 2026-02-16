package com.sseotdabwa.buyornot.feature.notification.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.sseotdabwa.buyornot.feature.notification.ui.NotificationScreen

const val NOTIFICATION_ROUTE = "notification"

fun NavGraphBuilder.notificationScreen() {
    composable(route = NOTIFICATION_ROUTE) {
        NotificationScreen()
    }
}

fun NavHostController.navigateToNotification(navOptions: NavOptions? = null) {
    this.navigate(NOTIFICATION_ROUTE, navOptions)
}
