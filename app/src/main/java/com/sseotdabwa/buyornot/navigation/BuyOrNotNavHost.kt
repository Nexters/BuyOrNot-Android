package com.sseotdabwa.buyornot.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.sseotdabwa.buyornot.BuildConfig
import com.sseotdabwa.buyornot.core.network.AuthEvent
import com.sseotdabwa.buyornot.core.network.AuthEventBus
import com.sseotdabwa.buyornot.core.ui.navigateToPrivacyPolicy
import com.sseotdabwa.buyornot.core.ui.navigateToTerms
import com.sseotdabwa.buyornot.core.ui.webViewScreen
import com.sseotdabwa.buyornot.feature.auth.navigation.SPLASH_ROUTE
import com.sseotdabwa.buyornot.feature.auth.navigation.authScreen
import com.sseotdabwa.buyornot.feature.auth.navigation.navigateForceToLogin
import com.sseotdabwa.buyornot.feature.auth.navigation.navigateToLogin
import com.sseotdabwa.buyornot.feature.auth.navigation.splashScreen
import com.sseotdabwa.buyornot.feature.home.navigation.homeScreen
import com.sseotdabwa.buyornot.feature.home.navigation.navigateToHome
import com.sseotdabwa.buyornot.feature.mypage.navigation.myPageGraph
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
    // 강제 로그아웃 이벤트 처리
    LaunchedEffect(authEventBus) {
        authEventBus.events.collect { event ->
            if (event == AuthEvent.FORCE_LOGOUT) {
                navController.navigateForceToLogin()
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
        )

        authScreen(
            onLoginSuccess = navController::navigateToHome,
            onTermsClick = navController::navigateToTerms,
            onPrivacyClick = navController::navigateToPrivacyPolicy,
        )

        homeScreen()
        uploadScreen(
            onNavigateBack = navController::popBackStack,
        )
        myPageGraph(
            navController = navController,
            versionName = BuildConfig.VERSION_NAME,
        )
        webViewScreen(
            onBackClick = navController::popBackStack,
        )
    }
}
