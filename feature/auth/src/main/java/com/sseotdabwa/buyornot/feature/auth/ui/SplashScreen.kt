package com.sseotdabwa.buyornot.feature.auth.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotAlertDialog
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotLotties
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

/**
 * 스플래시 화면의 네비게이션 진입점
 *
 * 앱 최초 진입 시 표시되는 스플래시 화면입니다.
 * 토큰 체크 + 업데이트 체크를 병렬로 실행하며,
 * 업데이트 팝업이 표시 중이면 다른 화면으로 이동하지 않습니다.
 *
 * @param onNavigateToLogin 로그인 화면으로 이동하는 콜백
 * @param onNavigateToHome 홈 화면으로 이동하는 콜백
 * @param onFinish 앱 종료 콜백 (강제 업데이트 시 "종료" 버튼)
 * @param viewModel SplashViewModel (Hilt 주입)
 */
@Composable
fun SplashRoute(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    onFinish: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is SplashSideEffect.NavigateToHome -> onNavigateToHome()
                is SplashSideEffect.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    SplashScreen(
        updateDialogType = uiState.updateDialogType,
        onDismissSoftUpdate = { viewModel.handleIntent(SplashIntent.DismissSoftUpdate) },
        onUpdateClick = { openPlayStore(context) },
        onFinish = onFinish,
    )
}

/**
 * 스플래시 화면 UI
 */
@Composable
private fun SplashScreen(
    updateDialogType: UpdateDialogType = UpdateDialogType.None,
    onDismissSoftUpdate: () -> Unit = {},
    onUpdateClick: () -> Unit = {},
    onFinish: () -> Unit = {},
) {
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

    when (updateDialogType) {
        UpdateDialogType.Force -> {
            BuyOrNotAlertDialog(
                onDismissRequest = { },
                title = "필수 업데이트가 있어요",
                subText = "서비스 이용을 위해 업데이트가 필요해요.",
                confirmText = "업데이트",
                dismissText = "종료",
                onConfirm = onUpdateClick,
                onDismiss = onFinish,
            )
        }
        UpdateDialogType.Soft -> {
            BuyOrNotAlertDialog(
                onDismissRequest = onDismissSoftUpdate,
                title = "새 버전이 출시됐어요",
                subText = "더 나은 경험을 위해 업데이트를 권장해요.",
                confirmText = "업데이트",
                dismissText = "나중에",
                onConfirm = onUpdateClick,
                onDismiss = onDismissSoftUpdate,
            )
        }
        UpdateDialogType.None -> Unit
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

@Preview(name = "SplashScreen - Pixel 5", device = "id:pixel_5", showBackground = true)
@Composable
private fun SplashScreenPreview() {
    BuyOrNotTheme {
        SplashScreen()
    }
}
