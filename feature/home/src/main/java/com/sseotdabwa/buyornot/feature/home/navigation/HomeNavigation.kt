package com.sseotdabwa.buyornot.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.sseotdabwa.buyornot.feature.home.ui.HomeScreen

const val HOME_ROUTE = "home"

fun NavGraphBuilder.homeScreen(
    onLoginClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
    composable(route = HOME_ROUTE) {
        HomeScreen(
            onLoginClick = onLoginClick,
            onNotificationClick = onNotificationClick,
            onProfileClick = onProfileClick,
        )
    }
}

fun NavHostController.navigateToHome(navOptions: NavOptions? = null) {
    this.navigate(HOME_ROUTE, navOptions)
}
