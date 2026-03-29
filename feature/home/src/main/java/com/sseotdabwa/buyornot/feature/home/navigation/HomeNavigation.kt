package com.sseotdabwa.buyornot.feature.home.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sseotdabwa.buyornot.feature.home.ui.HomeRoute
import com.sseotdabwa.buyornot.feature.home.ui.HomeTab

const val HOME_ROUTE = "home"
const val HOME_ROUTE_WITH_TAB = "home?tab={tab}"

fun NavGraphBuilder.homeScreen(
    onLoginClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onUploadClick: () -> Unit = {},
) {
    composable(
        route = HOME_ROUTE_WITH_TAB,
        arguments =
            listOf(
                navArgument("tab") {
                    type = NavType.StringType
                    nullable = true
                },
            ),
        enterTransition = {
            slideInVertically(
                initialOffsetY = { (it * 0.15f).toInt() },
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            ) + fadeIn(animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing))
        },
        popExitTransition = {
            slideOutVertically(
                targetOffsetY = { (it * 0.15f).toInt() },
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            ) + fadeOut(animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing))
        },
    ) { backStackEntry ->
        val tabName = backStackEntry.arguments?.getString("tab")
        val initialTab =
            when (tabName) {
                HomeTab.MY_FEED.name -> HomeTab.MY_FEED
                else -> HomeTab.FEED
            }

        HomeRoute(
            onLoginClick = onLoginClick,
            onNotificationClick = onNotificationClick,
            onProfileClick = onProfileClick,
            onUploadClick = onUploadClick,
            initialTab = initialTab,
        )
    }
}

fun NavHostController.navigateToHome(navOptions: NavOptions? = null) {
    this.navigate(HOME_ROUTE, navOptions)
}

fun NavHostController.navigateToHomeWithTab(
    tab: HomeTab,
    navOptions: NavOptions? = null,
) {
    this.navigate("home?tab=${tab.name}", navOptions)
}
