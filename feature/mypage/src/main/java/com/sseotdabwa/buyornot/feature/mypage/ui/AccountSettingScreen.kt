package com.sseotdabwa.buyornot.feature.mypage.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.sseotdabwa.buyornot.core.designsystem.components.BackTopBarWithTitle
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotConfirmDialog
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.core.ui.snackbar.LocalSnackbarState
import com.sseotdabwa.buyornot.domain.model.UserProfile
import com.sseotdabwa.buyornot.feature.mypage.components.SettingItem
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.AccountSettingIntent
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.AccountSettingSideEffect
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.AccountSettingUiState
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.AccountSettingViewModel

@Composable
fun AccountSettingRoute(
    onBackClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToWithdrawal: () -> Unit,
    viewModel: AccountSettingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarState = LocalSnackbarState.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is AccountSettingSideEffect.NavigateToLogin -> onNavigateToLogin()
                is AccountSettingSideEffect.ShowSnackbar -> {
                    snackbarState.show(
                        message = sideEffect.message,
                        icon = sideEffect.icon,
                        iconTint = sideEffect.iconTint,
                    )
                }
            }
        }
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    } else {
        AccountSettingScreen(
            onBackClick = onBackClick,
            onLogoutClick = {
                viewModel.handleIntent(AccountSettingIntent.Logout(context))
            },
            onShowLogoutDialog = {
                viewModel.handleIntent(AccountSettingIntent.ShowLogoutDialog)
            },
            onDismissLogoutDialog = {
                viewModel.handleIntent(AccountSettingIntent.DismissLogoutDialog)
            },
            onNavigateToWithdrawal = onNavigateToWithdrawal,
            uiState = uiState,
        )
    }
}

@Composable
fun AccountSettingScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onShowLogoutDialog: () -> Unit,
    onDismissLogoutDialog: () -> Unit,
    onNavigateToWithdrawal: () -> Unit,
    uiState: AccountSettingUiState,
) {
    Column(modifier = modifier.fillMaxSize()) {
        BackTopBarWithTitle(
            title = "계정 설정",
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier.padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EmailItem(uiState.userProfile?.email ?: "...")
            SettingItem("로그아웃") { onShowLogoutDialog() }
            SettingItem(
                title = "회원 탈퇴",
                textColor = BuyOrNotTheme.colors.red100,
            ) {
                onNavigateToWithdrawal()
            }
        }
    }

    if (uiState.isLogoutDialogVisible) {
        BuyOrNotConfirmDialog(
            onDismissRequest = onDismissLogoutDialog,
            title = "로그아웃 하시겠어요?",
            confirmText = "유지하기",
            dismissText = "로그아웃",
            onConfirm = onDismissLogoutDialog,
            onDismiss = {
                onLogoutClick()
                onDismissLogoutDialog()
            },
        )
    }
}

@Composable
private fun EmailItem(email: String) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "이메일",
            style = BuyOrNotTheme.typography.paragraphP1Medium,
            color = BuyOrNotTheme.colors.gray950,
        )

        Text(
            text = email,
            style = BuyOrNotTheme.typography.paragraphP2Medium,
            color = BuyOrNotTheme.colors.gray600,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AccountSettingScreenPreview() {
    BuyOrNotTheme {
        Scaffold(
            containerColor = BuyOrNotTheme.colors.gray0,
        ) { paddingValues ->
            AccountSettingScreen(
                modifier = Modifier.padding(paddingValues),
                onBackClick = {},
                onLogoutClick = {},
                onShowLogoutDialog = {},
                onDismissLogoutDialog = {},
                onNavigateToWithdrawal = {},
                uiState =
                    AccountSettingUiState(
                        userProfile =
                            UserProfile(
                                id = 0,
                                nickname = "서따봐",
                                profileImage = "",
                                socialAccount = "KAKAO",
                                email = "buyornot@gmail.com",
                            ),
                    ),
            )
        }
    }
}
