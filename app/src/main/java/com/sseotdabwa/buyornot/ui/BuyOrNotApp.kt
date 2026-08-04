package com.sseotdabwa.buyornot.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sseotdabwa.buyornot.PendingFeedDeepLink
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotSnackBarHost
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.core.network.AuthEventBus
import com.sseotdabwa.buyornot.core.ui.performance.LocalScreenRenderReporter
import com.sseotdabwa.buyornot.core.ui.performance.ScreenRenderReporter
import com.sseotdabwa.buyornot.core.ui.permission.rememberNotificationPermission
import com.sseotdabwa.buyornot.core.ui.snackbar.LocalSnackbarState
import com.sseotdabwa.buyornot.core.ui.snackbar.rememberBuyOrNotSnackbarState
import com.sseotdabwa.buyornot.feature.auth.navigation.AuthRoute
import com.sseotdabwa.buyornot.feature.auth.navigation.SplashRoute
import com.sseotdabwa.buyornot.feature.home.navigation.HomeRoute
import com.sseotdabwa.buyornot.feature.notification.navigation.navigateToFeedDetail
import com.sseotdabwa.buyornot.navigation.BuyOrNotNavHost
import com.sseotdabwa.buyornot.performance.ScreenPerformanceTracker
import com.sseotdabwa.buyornot.performance.screenTraceNameOf

@Composable
fun BuyOrNotApp(
    authEventBus: AuthEventBus,
    screenPerformanceTracker: ScreenPerformanceTracker,
    pendingFeedDeepLink: PendingFeedDeepLink? = null,
    onPendingFeedDeepLinkConsumed: () -> Unit = {},
    onBackPressed: () -> Unit = {},
    onFinish: () -> Unit = {},
    viewModel: BuyOrNotViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val snackbarState = rememberBuyOrNotSnackbarState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isFirstRun by viewModel.isFirstRun.collectAsStateWithLifecycle()

    BackHandler(enabled = currentDestination?.route?.startsWith(HomeRoute::class.qualifiedName ?: "") == true) {
        onBackPressed()
    }

    val (hasNotificationPermission, requestNotificationPermission) = rememberNotificationPermission()

    LaunchedEffect(isFirstRun) {
        if (isFirstRun) {
            if (!hasNotificationPermission) {
                requestNotificationPermission()
            }
            viewModel.updateIsFirstRun(false)
        }
    }

    // FCM 알림 탭으로 전달된 pending feedId를 인증 완료(Splash/Auth 통과) 후 한 번만 소비한다.
    // Splash/로그인 화면에서는 보류하고, 인증된 어떤 화면(Home·MyPage·Upload 등)에서든 즉시 이동한다.
    val currentRoute = currentDestination?.route
    val isPastAuthGate =
        currentRoute != null &&
            currentRoute != SplashRoute::class.qualifiedName &&
            currentRoute != AuthRoute::class.qualifiedName
    LaunchedEffect(pendingFeedDeepLink, isPastAuthGate) {
        val deepLink = pendingFeedDeepLink ?: return@LaunchedEffect
        if (!isPastAuthGate) return@LaunchedEffect
        navController.navigateToFeedDetail(deepLink.feedId, deepLink.notificationId)
        onPendingFeedDeepLinkConsumed()
    }

    val isFullscreen =
        currentDestination?.route.let { route ->
            route == SplashRoute::class.qualifiedName || route == AuthRoute::class.qualifiedName
        }

    // 화면별 렌더링 지표. route에는 패키지 경로와 인자가 섞여 있어 짧은 이름으로 접어서 쓴다.
    val screenName = remember(currentRoute) { screenTraceNameOf(currentRoute) }
    LaunchedEffect(screenName) {
        screenName?.let(screenPerformanceTracker::onScreenEntered)
    }

    // 스플래시가 아닌 첫 화면의 콘텐츠가 그려진 시점을 앱 시작 TTFD로 보고한다.
    // Firebase `_app_start`는 Activity onResume에서 끝나 Compose 첫 프레임 전이므로 체감 시간을
    // 과소보고한다. reportFullyDrawn()은 Activity당 한 번만 유효해 화면 전환에는 쓸 수 없다.
    ReportDrawnWhen { screenPerformanceTracker.isFirstMeaningfulRenderDone }

    val screenRenderReporter =
        remember(screenPerformanceTracker) {
            ScreenRenderReporter { screenPerformanceTracker.onScreenContentRendered() }
        }

    CompositionLocalProvider(
        LocalSnackbarState provides snackbarState,
        LocalScreenRenderReporter provides screenRenderReporter,
    ) {
        Scaffold(
            containerColor = BuyOrNotTheme.colors.gray0,
            snackbarHost = { BuyOrNotSnackBarHost(snackbarState.snackbarHostState) },
        ) { innerPadding ->
            BuyOrNotNavHost(
                navController = navController,
                authEventBus = authEventBus,
                onFinish = onFinish,
                modifier =
                    Modifier
                        .consumeWindowInsets(innerPadding)
                        .bottomBarPadding(isFullscreen, innerPadding),
            )
        }
    }
}

private fun Modifier.bottomBarPadding(
    isFullscreen: Boolean,
    padding: PaddingValues,
): Modifier =
    if (isFullscreen) {
        this
    } else {
        this.padding(padding)
    }
