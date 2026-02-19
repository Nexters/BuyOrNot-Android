package com.sseotdabwa.buyornot.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.sseotdabwa.buyornot.BuildConfig
import com.sseotdabwa.buyornot.core.network.AuthEvent
import com.sseotdabwa.buyornot.core.network.AuthEventBus
import com.sseotdabwa.buyornot.core.ui.snackbar.LocalSnackbarState
import com.sseotdabwa.buyornot.core.ui.webview.navigateToPrivacyPolicy
import com.sseotdabwa.buyornot.core.ui.webview.navigateToTerms
import com.sseotdabwa.buyornot.core.ui.webview.webViewScreen
import com.sseotdabwa.buyornot.feature.auth.navigation.SPLASH_ROUTE
import com.sseotdabwa.buyornot.feature.auth.navigation.authScreen
import com.sseotdabwa.buyornot.feature.auth.navigation.navigateForceToLogin
import com.sseotdabwa.buyornot.feature.auth.navigation.navigateToLogin
import com.sseotdabwa.buyornot.feature.auth.navigation.splashScreen
import com.sseotdabwa.buyornot.feature.home.navigation.homeScreen
import com.sseotdabwa.buyornot.feature.home.navigation.navigateToHome
import com.sseotdabwa.buyornot.feature.mypage.navigation.myPageGraph
import com.sseotdabwa.buyornot.feature.mypage.navigation.navigateToMyPage
import com.sseotdabwa.buyornot.feature.notification.navigation.navigateToNotification
import com.sseotdabwa.buyornot.feature.notification.navigation.navigateToNotificationDetail
import com.sseotdabwa.buyornot.feature.notification.navigation.navigateToNotificationDetail
import com.sseotdabwa.buyornot.feature.notification.navigation.notificationGraph
import com.sseotdabwa.buyornot.feature.upload.navigation.navigateToUpload
import com.sseotdabwa.buyornot.feature.upload.navigation.uploadScreen

/**
 * BuyOrNot 앱의 메인 네비게이션 호스트
 *
 * @param navController 네비게이션 컨트롤러
 * @param authEventBus 인증 관련 글로벌 이벤트를 수신하는 버스
 * @param modifier 레이아웃 수정자
 */
@Composable
fun BuyOrNotNavHost(
    navController: NavHostController,
    authEventBus: AuthEventBus,
    modifier: Modifier = Modifier,
) {
    val snackbarState = LocalSnackbarState.current

    // 강제 로그아웃 이벤트 처리
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
        startDestination = SPLASH_ROUTE,
        modifier = modifier,
    ) {
        splashScreen(
            onNavigateToLogin = navController::navigateToLogin,
            onNavigateToHome = {
                navController.navigateToHome(
                    navOptions = androidx.navigation.navOptions {
                        popUpTo(SPLASH_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                )
            },
        )

        authScreen(
            onLoginSuccess = {
                navController.navigateToHome(
                    navOptions = androidx.navigation.navOptions {
                        popUpTo(SPLASH_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                )
            },
            onTermsClick = navController::navigateToTerms,
            onPrivacyClick = navController::navigateToPrivacyPolicy,
        )

        homeScreen(
            onLoginClick = {
                navController.navigateForceToLogin()
            },
            onNotificationClick = {
                navController.navigateToNotification()
            },
            onProfileClick = {
                navController.navigateToMyPage()
            },
            onUploadClick = {
                navController.navigateToUpload()
            },
        )
        notificationGraph(
            onBackClick = navController::popBackStack,
            onNotificationClick = navController::navigateToNotificationDetail,
        )
        uploadScreen(
            onNavigateBack = navController::popBackStack,
        )
        myPageGraph(
            navController = navController,
            versionName = BuildConfig.VERSION_NAME,
            onNavigateToLogin = navController::navigateForceToLogin,
        )
        webViewScreen(
            onBackClick = navController::popBackStack,
        )
    }
}
