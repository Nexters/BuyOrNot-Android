package com.sseotdabwa.buyornot.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.sseotdabwa.buyornot.BuildConfig
import com.sseotdabwa.buyornot.core.common.deeplink.feedShareTextOf
import com.sseotdabwa.buyornot.core.network.AuthEvent
import com.sseotdabwa.buyornot.core.network.AuthEventBus
import com.sseotdabwa.buyornot.core.ui.crop.editScreen
import com.sseotdabwa.buyornot.core.ui.imageviewer.imageViewerScreen
import com.sseotdabwa.buyornot.core.ui.imageviewer.navigateToImageViewer
import com.sseotdabwa.buyornot.core.ui.snackbar.LocalSnackbarState
import com.sseotdabwa.buyornot.core.ui.webview.navigateToPrivacyPolicy
import com.sseotdabwa.buyornot.core.ui.webview.navigateToTerms
import com.sseotdabwa.buyornot.core.ui.webview.navigateToWebView
import com.sseotdabwa.buyornot.core.ui.webview.webViewScreen
import com.sseotdabwa.buyornot.feature.auth.navigation.AuthRoute
import com.sseotdabwa.buyornot.feature.auth.navigation.SplashRoute
import com.sseotdabwa.buyornot.feature.auth.navigation.authScreen
import com.sseotdabwa.buyornot.feature.auth.navigation.navigateForceToLogin
import com.sseotdabwa.buyornot.feature.auth.navigation.navigateToLogin
import com.sseotdabwa.buyornot.feature.auth.navigation.splashScreen
import com.sseotdabwa.buyornot.feature.home.navigation.homeScreen
import com.sseotdabwa.buyornot.feature.home.navigation.navigateToHome
import com.sseotdabwa.buyornot.feature.home.navigation.navigateToHomeWithTab
import com.sseotdabwa.buyornot.feature.home.ui.HomeTab
import com.sseotdabwa.buyornot.feature.mypage.navigation.myPageGraph
import com.sseotdabwa.buyornot.feature.mypage.navigation.navigateToMyPage
import com.sseotdabwa.buyornot.feature.notification.navigation.navigateToNotification
import com.sseotdabwa.buyornot.feature.notification.navigation.navigateToNotificationDetail
import com.sseotdabwa.buyornot.feature.notification.navigation.notificationGraph
import com.sseotdabwa.buyornot.feature.upload.navigation.UploadRoute
import com.sseotdabwa.buyornot.feature.upload.navigation.navigateToUpload
import com.sseotdabwa.buyornot.feature.upload.navigation.uploadScreen

@Composable
fun BuyOrNotNavHost(
    navController: NavHostController,
    authEventBus: AuthEventBus,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarState = LocalSnackbarState.current
    val context = LocalContext.current

    // 공유 URL의 host는 buildType별로 주입되는 BuildConfig.APP_LINK_HOST를 쓴다.
    // 이 값이 app 모듈에만 있어 실행을 여기서 한다 — onLinkClick·onImageClick과 같은 층이다.
    val shareFeed: (Long, String) -> Unit = { feedId, title ->
        val sendIntent =
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    feedShareTextOf(BuildConfig.APP_LINK_HOST, feedId, title),
                )
            }
        context.startActivity(Intent.createChooser(sendIntent, null))
    }

    LaunchedEffect(authEventBus) {
        authEventBus.events.collect { event ->
            if (event == AuthEvent.FORCE_LOGOUT) {
                navController.navigateForceToLogin()
                snackbarState.show(
                    message = "원활한 서비스 이용을 위해 로그인이 필요합니다.",
                )
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = SplashRoute,
        modifier = modifier,
    ) {
        splashScreen(
            onNavigateToLogin = navController::navigateToLogin,
            onNavigateToHome = {
                navController.navigateToHome(
                    navOptions =
                        androidx.navigation.navOptions {
                            popUpTo<SplashRoute> { inclusive = true }
                            launchSingleTop = true
                        },
                )
            },
            onFinish = onFinish,
        )

        authScreen(
            onLoginSuccess = {
                navController.navigateToHome(
                    navOptions =
                        androidx.navigation.navOptions {
                            popUpTo<AuthRoute> { inclusive = true }
                            launchSingleTop = true
                        },
                )
            },
            onTermsClick = navController::navigateToTerms,
            onPrivacyClick = navController::navigateToPrivacyPolicy,
        )

        homeScreen(
            onLoginClick = navController::navigateForceToLogin,
            onNotificationClick = navController::navigateToNotification,
            onProfileClick = navController::navigateToMyPage,
            onUploadClick = navController::navigateToUpload,
            onLinkClick = { url -> navController.navigateToWebView("", url) },
            onShareClick = shareFeed,
            onImageClick = { urls, page -> navController.navigateToImageViewer(urls, page) },
        )
        notificationGraph(
            onBackClick = navController::popBackStack,
            onNotificationClick = navController::navigateToNotificationDetail,
            onLinkClick = { url -> navController.navigateToWebView("", url) },
            onShareClick = shareFeed,
            onImageClick = { urls, page -> navController.navigateToImageViewer(urls, page) },
        )
        uploadScreen(
            navController = navController,
            onNavigateBack = navController::popBackStack,
            onNavigateToHomeReview = {
                navController.navigateToHomeWithTab(
                    tab = HomeTab.MY_FEED,
                    navOptions =
                        androidx.navigation.navOptions {
                            popUpTo<UploadRoute> {
                                inclusive = true
                            }
                            launchSingleTop = true
                        },
                )
            },
        )
        myPageGraph(
            navController = navController,
            versionName = BuildConfig.VERSION_NAME,
            onNavigateToLogin = navController::navigateForceToLogin,
        )
        imageViewerScreen(
            onBackClick = navController::popBackStack,
        )
        webViewScreen(
            onBackClick = navController::popBackStack,
        )
        editScreen(navController = navController)
    }
}
