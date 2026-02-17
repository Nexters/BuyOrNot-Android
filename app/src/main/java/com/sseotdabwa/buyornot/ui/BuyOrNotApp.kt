package com.sseotdabwa.buyornot.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotSnackBarHost
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.core.network.AuthEventBus
import com.sseotdabwa.buyornot.core.ui.LocalSnackbarState
import com.sseotdabwa.buyornot.core.ui.rememberBuyOrNotSnackbarState
import com.sseotdabwa.buyornot.feature.auth.navigation.AUTH_ROUTE
import com.sseotdabwa.buyornot.feature.auth.navigation.SPLASH_ROUTE
import com.sseotdabwa.buyornot.feature.home.navigation.HOME_ROUTE
import com.sseotdabwa.buyornot.navigation.BuyOrNotNavHost

/**
 * BuyOrNot 앱의 메인 컴포저블
 *
 * 네비게이션과 하단 네비게이션 바를 포함한 앱의 전체 구조를 정의합니다.
 * 스플래시 및 로그인 화면에서는 하단 바가 표시되지 않습니다.
 *
 *
 * 전체 화면이 필요하면 → bottomBarPadding() 함수의 리스트에 라우트 추가
 *
 * 일반 화면이면 → 아무 것도 하지 않아도 자동으로 패딩 적용
 *
 */
@Composable
fun BuyOrNotApp(authEventBus: AuthEventBus) {
    val navController = rememberNavController()
    val snackbarState = rememberBuyOrNotSnackbarState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    CompositionLocalProvider(LocalSnackbarState provides snackbarState) {
        Scaffold(
            containerColor = BuyOrNotTheme.colors.gray0,
            snackbarHost = { BuyOrNotSnackBarHost(snackbarState.snackbarHostState) },
        ) { innerPadding ->
            BuyOrNotNavHost(
                navController = navController,
                authEventBus = authEventBus,
                modifier =
                    Modifier
                        .consumeWindowInsets(innerPadding)
                        .bottomBarPadding(currentDestination, innerPadding),
            )
        }
    }
}

/**
 * 특정 화면(스플래시, 로그인)에서는 시스템 패딩을 제거하는 확장 함수
 *
 * NavHost에 적용되어, 전체 화면이 필요한 스플래시/로그인 화면에서는
 * 시스템 바 영역까지 확장되고, 일반 화면에서는 하단 바 패딩을 적용합니다.
 *
 * @param currentDestination 현재 네비게이션 목적지
 * @param padding Scaffold의 innerPadding (하단 바 높이 포함)
 * @return 조건에 따라 패딩이 적용되거나 제거된 Modifier
 */
private fun Modifier.bottomBarPadding(
    currentDestination: NavDestination?,
    padding: PaddingValues,
): Modifier =
    if (currentDestination?.route in listOf(SPLASH_ROUTE, AUTH_ROUTE, HOME_ROUTE)) {
        this
    } else {
        this.padding(padding)
    }
