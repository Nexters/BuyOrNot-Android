package com.sseotdabwa.buyornot.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.sseotdabwa.buyornot.BuildConfig
import com.sseotdabwa.buyornot.feature.auth.navigation.authScreen
import com.sseotdabwa.buyornot.feature.auth.navigation.navigateToLogin
import com.sseotdabwa.buyornot.feature.auth.navigation.splashScreen
import com.sseotdabwa.buyornot.feature.home.navigation.homeScreen
import com.sseotdabwa.buyornot.feature.mypage.navigation.MyPageScreens
import com.sseotdabwa.buyornot.feature.mypage.navigation.myPageGraph
import com.sseotdabwa.buyornot.feature.upload.navigation.uploadScreen

/**
 * BuyOrNot 앱의 메인 네비게이션 호스트
 *
 * 앱의 모든 화면 네비게이션을 관리합니다.
 * 시작 화면은 스플래시 화면이며, 자동으로 로그인 화면으로 전환됩니다.
 *
 * @param navController 네비게이션 컨트롤러
 * @param modifier 레이아웃 수정자
 */
@Composable
fun BuyOrNotNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = MyPageScreens.Graph.route,
        modifier = modifier,
    ) {
        // 스플래시 화면 - 앱 시작점
        splashScreen(
            onNavigateToLogin = { navController.navigateToLogin() },
        )

        // 인증 화면 - 로그인
        authScreen(
            onGoogleLoginClick = {
                // TODO: 구글 로그인 후 홈으로 이동
            },
            onKakaoLoginClick = {
                // TODO: 카카오 로그인 후 홈으로 이동
            },
        )

        // 메인 화면들
        homeScreen()
        uploadScreen(
            onNavigateBack = { navController.popBackStack() },
        )
        myPageGraph(
            navController = navController,
            versionName = BuildConfig.VERSION_NAME,
            onNavigateBack = { navController.popBackStack() },
            onAccountSettingClick = { navController.navigate(MyPageScreens.AccountInfo.route) },
            onPolicyClick = { navController.navigate(MyPageScreens.Policy.route) },
        )
    }
}
