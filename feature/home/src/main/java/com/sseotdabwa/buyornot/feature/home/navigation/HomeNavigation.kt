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
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.sseotdabwa.buyornot.feature.home.ui.HomeTab
import kotlinx.serialization.Serializable
import com.sseotdabwa.buyornot.feature.home.ui.HomeRoute as HomeScreen

@Serializable
data class HomeRoute(
    val tab: String? = null,
)

fun NavGraphBuilder.homeScreen(
    onLoginClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onUploadClick: () -> Unit = {},
    onLinkClick: (url: String) -> Unit = {},
    onImageClick: (imageUrls: List<String>, page: Int) -> Unit = { _, _ -> },
) {
    composable<HomeRoute>(
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
        val route = backStackEntry.toRoute<HomeRoute>()
        val initialTab =
            when (route.tab) {
                HomeTab.MY_FEED.name -> HomeTab.MY_FEED
                else -> HomeTab.FEED
            }

        HomeScreen(
            onLoginClick = onLoginClick,
            onNotificationClick = onNotificationClick,
            onProfileClick = onProfileClick,
            onUploadClick = onUploadClick,
            onLinkClick = onLinkClick,
            onImageClick = onImageClick,
            initialTab = initialTab,
        )
    }
}

fun NavHostController.navigateToHome(navOptions: NavOptions? = null) {
    navigate(HomeRoute(), navOptions)
}

fun NavHostController.navigateToHomeWithTab(
    tab: HomeTab,
    navOptions: NavOptions? = null,
) {
    navigate(HomeRoute(tab = tab.name), navOptions)
}
