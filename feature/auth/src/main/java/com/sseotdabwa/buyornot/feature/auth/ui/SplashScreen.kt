package com.sseotdabwa.buyornot.feature.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import kotlinx.coroutines.delay

/**
 * 스플래시 화면 관련 상수
 */
private object SplashConstants {
    const val SPLASH_TIMEOUT_MILLIS = 2000L
    val APP_LOGO_SIZE = 120.dp
}

/**
 * 스플래시 화면의 네비게이션 진입점
 *
 * 앱 최초 진입 시 표시되는 스플래시 화면입니다.
 * 지정된 시간(2초) 후 자동으로 로그인 화면으로 이동합니다.
 *
 * @param onTimeout 스플래시 타임아웃 후 실행될 콜백 (로그인 화면으로 이동)
 */
@Composable
fun SplashRoute(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(SplashConstants.SPLASH_TIMEOUT_MILLIS)
        onTimeout()
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
        Image(
            painter = painterResource(id = BuyOrNotIcons.AppLogo.resId),
            contentDescription = "App Logo",
            modifier = Modifier.size(SplashConstants.APP_LOGO_SIZE),
        )
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
