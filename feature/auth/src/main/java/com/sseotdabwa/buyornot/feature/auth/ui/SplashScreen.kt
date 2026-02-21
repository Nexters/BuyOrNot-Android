package com.sseotdabwa.buyornot.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotLotties
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.feature.auth.viewmodel.SplashSideEffect

/**
 * 스플래시 화면의 네비게이션 진입점
 *
 * 앱 최초 진입 시 표시되는 스플래시 화면입니다.
 * 지정된 시간(2.3초) 후 자동으로 로그인 상태를 확인하여:
 * - 로그인 상태(토큰 있음) → 홈 화면으로 이동
 * - 비로그인 상태 → 로그인 화면으로 이동
 *
 * @param onNavigateToLogin 로그인 화면으로 이동하는 콜백
 * @param onNavigateToHome 홈 화면으로 이동하는 콜백
 * @param viewModel SplashViewModel (Hilt 주입)
 */
@Composable
fun SplashRoute(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    // SideEffect 처리 (ViewModel에서 토큰 체크 후 네비게이션 이벤트 방출)
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is SplashSideEffect.NavigateToHome -> onNavigateToHome()
                is SplashSideEffect.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    SplashScreen()
}

/**
 * 스플래시 화면 UI
 *
 * 앱 로고를 중앙에 표시합니다.
 */
@Composable
private fun SplashScreen() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(BuyOrNotTheme.colors.gray0),
        contentAlignment = Alignment.Center,
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(BuyOrNotLotties.SplashLoading.resId))

        if (composition != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LottieAnimation(
                    composition = composition,
                    iterations = Int.MAX_VALUE,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Icon(
                    imageVector = BuyOrNotIcons.AppLogo.asImageVector(),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(width = 160.dp, height = 40.dp),
                    tint = BuyOrNotTheme.colors.gray1000,
                )
            }
        }
    }
}

/**
 * 스플래시 화면 프리뷰
 */
@Preview(name = "SplashScreen - Pixel 5", device = "id:pixel_5", showBackground = true)
@Composable
private fun SplashScreenPreview() {
    BuyOrNotTheme {
        SplashScreen()
    }
}
