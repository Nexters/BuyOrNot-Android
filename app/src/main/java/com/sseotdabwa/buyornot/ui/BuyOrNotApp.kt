package com.sseotdabwa.buyornot.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotAlertDialog
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotSnackBarHost
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.core.network.AuthEventBus
import com.sseotdabwa.buyornot.core.ui.permission.rememberNotificationPermission
import com.sseotdabwa.buyornot.core.ui.snackbar.LocalSnackbarState
import com.sseotdabwa.buyornot.core.ui.snackbar.rememberBuyOrNotSnackbarState
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
 * @param authEventBus 인증 관련 이벤트 버스
 * @param onBackPressed 홈 화면에서 뒤로가기 시 앱 종료를 위한 콜백
 * @param onFinish 앱 강제 종료를 위한 콜백 (강제 업데이트 시 "종료" 버튼)
 * @param viewModel 앱 공통 ViewModel
 */
@Composable
fun BuyOrNotApp(
    authEventBus: AuthEventBus,
    onBackPressed: () -> Unit = {},
    onFinish: () -> Unit = {},
    viewModel: BuyOrNotViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val snackbarState = rememberBuyOrNotSnackbarState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isFirstRun by viewModel.isFirstRun.collectAsStateWithLifecycle()
    val updateDialogType by viewModel.updateDialogType.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 홈 화면에서 뒤로가기 시 앱 종료
    BackHandler(enabled = currentDestination?.route == HOME_ROUTE) {
        onBackPressed()
    }

    // 앱 진입 시 최초 1회만 알림 권한 자동 요청
    val (hasNotificationPermission, requestNotificationPermission) = rememberNotificationPermission()

    LaunchedEffect(isFirstRun) {
        if (isFirstRun) {
            if (!hasNotificationPermission) {
                requestNotificationPermission()
            }
            viewModel.updateIsFirstRun(false)
        }
    }

    when (updateDialogType) {
        UpdateDialogType.Force -> {
            BuyOrNotAlertDialog(
                onDismissRequest = { },
                title = "업데이트가 필요해요",
                subText = "앱을 계속 사용하려면 최신 버전으로 업데이트해 주세요.",
                confirmText = "업데이트",
                dismissText = "종료",
                onConfirm = { openPlayStore(context) },
                onDismiss = { onFinish() },
            )
        }
        UpdateDialogType.Soft -> {
            BuyOrNotAlertDialog(
                onDismissRequest = { viewModel.dismissSoftUpdate() },
                title = "새로운 버전이 있어요",
                subText = "더 나은 경험을 위해 업데이트를 권장합니다.",
                confirmText = "업데이트",
                dismissText = "나중에",
                onConfirm = { openPlayStore(context) },
                onDismiss = { viewModel.dismissSoftUpdate() },
            )
        }
        UpdateDialogType.None -> Unit
    }

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

private fun openPlayStore(context: Context) {
    val packageName = context.packageName
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")),
        )
    } catch (e: ActivityNotFoundException) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName"),
            ),
        )
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
    if (currentDestination?.route in listOf(SPLASH_ROUTE, AUTH_ROUTE)) {
        this
    } else {
        this.padding(padding)
    }
