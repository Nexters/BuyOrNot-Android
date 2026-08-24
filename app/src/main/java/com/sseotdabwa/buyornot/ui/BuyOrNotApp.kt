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
import androidx.navigation.navOptions
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
import com.sseotdabwa.buyornot.feature.home.navigation.navigateToHome
import com.sseotdabwa.buyornot.feature.notification.navigation.navigateToFeedDetail
import com.sseotdabwa.buyornot.feature.upload.navigation.navigateToUpload
import com.sseotdabwa.buyornot.navigation.BuyOrNotNavHost
import com.sseotdabwa.buyornot.notification.PendingPushNavigation
import com.sseotdabwa.buyornot.notification.PushDestination
import com.sseotdabwa.buyornot.performance.ScreenPerformanceTracker
import com.sseotdabwa.buyornot.performance.screenTraceNameOf

@Composable
fun BuyOrNotApp(
    authEventBus: AuthEventBus,
    screenPerformanceTracker: ScreenPerformanceTracker,
    pendingPushNavigation: PendingPushNavigation? = null,
    onPendingPushNavigationConsumed: () -> Unit = {},
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
    LaunchedEffect(pendingPushNavigation, isPastAuthGate) {
        val navigation = pendingPushNavigation ?: return@LaunchedEffect
        if (!isPastAuthGate) return@LaunchedEffect
        when (navigation.destination) {
            // feedId는 pushDestinationOf가 FEED_DETAIL일 때 존재를 보장한다.
            PushDestination.FEED_DETAIL ->
                navigation.feedId?.let { navController.navigateToFeedDetail(it, navigation.notificationId) }

            PushDestination.FEED_CREATE -> navController.navigateToUpload()

            // 인증 통과 직후엔 이미 홈이지만, 다른 화면에 있다가 탭한 경우엔 홈으로 되돌려야 한다.
            // 홈이 중복으로 쌓이지 않게 기존 홈을 걷어내고 하나만 남긴다.
            PushDestination.HOME ->
                navController.navigateToHome(
                    navOptions {
                        popUpTo<HomeRoute> { inclusive = true }
                        launchSingleTop = true
                    },
                )
        }
        onPendingPushNavigationConsumed()
    }

    val isFullscreen =
        currentDestination?.route.let { route ->
            route == SplashRoute::class.qualifiedName || route == AuthRoute::class.qualifiedName
        }

    // 화면별 렌더링 지표. route에는 패키지 경로와 인자 자리표시자가 섞여 있어 짧은 이름으로 접어서 쓴다.
    //
    // 체류 구간은 이름이 아니라 back stack entry로 가른다. route는 인자가 채워지지 않은 패턴
    // (`...NotificationDetailRoute/{feedId}/{notificationId}`)이라 피드 A 상세와 B 상세가
    // 완전히 같은 문자열이다. 이름을 기준으로 삼으면 A → B 진입에서 B의 Trace가 열리지 않는다.
    val screenName = remember(currentRoute) { screenTraceNameOf(currentRoute) }
    val screenSessionId = navBackStackEntry?.id
    LaunchedEffect(screenSessionId, screenName) {
        val screen = screenName ?: return@LaunchedEffect
        val sessionId = screenSessionId ?: return@LaunchedEffect
        screenPerformanceTracker.onScreenEntered(screen = screen, sessionId = sessionId)
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
