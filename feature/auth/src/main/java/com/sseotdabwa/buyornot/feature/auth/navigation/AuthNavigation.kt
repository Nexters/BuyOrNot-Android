package com.sseotdabwa.buyornot.feature.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.sseotdabwa.buyornot.feature.auth.ui.AuthRoute
import com.sseotdabwa.buyornot.feature.auth.ui.SplashRoute

/**
 * 스플래시 화면의 네비게이션 라우트 상수
 */
const val SPLASH_ROUTE = "splash"

/**
 * 인증(로그인) 화면의 네비게이션 라우트 상수
 *
 * NavHost에서 인증 화면으로 이동할 때 사용되는 라우트 문자열입니다.
 */
const val AUTH_ROUTE = "auth"

/**
 * NavGraphBuilder의 확장 함수 - 스플래시 화면을 네비게이션 그래프에 추가
 *
 * 앱 최초 진입 시 표시되는 스플래시 화면을 등록합니다.
 * 2초 후 자동으로 로그인 화면으로 이동합니다.
 *
 * @param onNavigateToLogin 스플래시 타임아웃 후 로그인 화면으로 이동할 때 실행될 콜백
 */
fun NavGraphBuilder.splashScreen(onNavigateToLogin: () -> Unit) {
    composable(route = SPLASH_ROUTE) {
        SplashRoute(
            onTimeout = onNavigateToLogin,
        )
    }
}

/**
 * NavGraphBuilder의 확장 함수 - 인증 화면을 네비게이션 그래프에 추가
 *
 * Compose Navigation을 사용하여 인증(로그인) 화면을 네비게이션 그래프에 등록합니다.
 * 소셜 로그인 기능을 포함한 로그인 화면이 표시되며,
 * 약관 및 개인정보처리방침은 AuthRoute 내부에서 UriHandler로 처리됩니다.
 *
 * 사용 예시:
 * ```
 * NavHost(navController = navController, startDestination = SPLASH_ROUTE) {
 *     splashScreen(
 *         onNavigateToLogin = { navController.navigate(AUTH_ROUTE) }
 *     )
 *     authScreen(
 *         onGoogleLoginClick = { /* 구글 로그인 처리 */ },
 *         onKakaoLoginClick = { /* 카카오 로그인 처리 */ }
 *     )
 * }
 * ```
 *
 * @param onGoogleLoginClick 구글 로그인 버튼 클릭 시 실행될 콜백
 * @param onKakaoLoginClick 카카오 로그인 버튼 클릭 시 실행될 콜백
 * @param onTermsClick 서비스 약관 링크 클릭 콜백
 * @param onPrivacyClick 개인정보처리방침 링크 클릭 콜백
 */
fun NavGraphBuilder.authScreen(
    onGoogleLoginClick: () -> Unit = {},
    onKakaoLoginClick: () -> Unit = {},
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
) {
    composable(route = AUTH_ROUTE) {
        AuthRoute(
            onGoogleLoginClick = onGoogleLoginClick,
            onKakaoLoginClick = onKakaoLoginClick,
            onTermsClick = onTermsClick,
            onPrivacyClick = onPrivacyClick,
        )
    }
}

/**
 * 스플래시에서 로그인 화면으로 이동하는 확장 함수
 *
 * popUpTo를 사용하여 스플래시 화면을 백스택에서 제거합니다.
 * 사용자가 로그인 화면에서 뒤로가기 버튼을 눌러도 스플래시로 돌아가지 않도록 합니다.
 */
fun NavHostController.navigateToLogin() {
    navigate(AUTH_ROUTE) {
        popUpTo(SPLASH_ROUTE) {
            inclusive = true
        }
    }
}
