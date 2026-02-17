package com.sseotdabwa.buyornot.feature.mypage.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sseotdabwa.buyornot.core.designsystem.components.BackTopBarWithTitle
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotConfirmDialog
import com.sseotdabwa.buyornot.core.designsystem.components.PrimaryButton
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotImgs
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.core.ui.snackbar.LocalSnackbarState
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.WithdrawalIntent
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.WithdrawalSideEffect
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.WithdrawalUiState
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.WithdrawalViewModel

@Composable
fun WithdrawalRoute(
    onBackClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: WithdrawalViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarState = LocalSnackbarState.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is WithdrawalSideEffect.NavigateToLogin -> onNavigateToLogin()
                is WithdrawalSideEffect.ShowSnackbar -> {
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
        WithdrawalScreen(
            onBackClick = onBackClick,
            onWithdrawalClick = {
                viewModel.handleIntent(WithdrawalIntent.Withdraw(context))
            },
            uiState = uiState,
        )
    }
}

@Composable
fun WithdrawalScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onWithdrawalClick: () -> Unit,
    uiState: WithdrawalUiState,
) {
    var isWithdrawalDialogVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        BackTopBarWithTitle(
            title = "회원탈퇴",
            onBackClick = onBackClick,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(20.dp),
        ) {
            Text(
                text = "${uiState.userProfile?.nickname ?: "..."}님,\n살까말까를 떠나시나요?",
                style = BuyOrNotTheme.typography.headingH3Bold,
                color = BuyOrNotTheme.colors.gray900,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "지금까지의 투표들이 전부 사라져요 :(",
                style = BuyOrNotTheme.typography.paragraphP1Medium,
                color = BuyOrNotTheme.colors.gray700,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Image(
                painter = painterResource(id = BuyOrNotImgs.WithdrawalBackground.resId),
                contentDescription = null,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
            )

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                "탈퇴하기",
                modifier = Modifier.fillMaxWidth(),
            ) {
                isWithdrawalDialogVisible = true
            }
        }
    }

    if (isWithdrawalDialogVisible) {
        BuyOrNotConfirmDialog(
            onDismissRequest = { isWithdrawalDialogVisible = false },
            title = "정말 탈퇴하시겠어요?",
            confirmText = "유지하기",
            dismissText = "탈퇴하기",
            onConfirm = { isWithdrawalDialogVisible = false },
            onDismiss = {
                onWithdrawalClick()
                isWithdrawalDialogVisible = false
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WithdrawalScreenPreview() {
    BuyOrNotTheme {
        Scaffold(
            containerColor = BuyOrNotTheme.colors.gray0,
        ) { paddingValues ->
            WithdrawalScreen(
                modifier = Modifier.padding(paddingValues),
                onBackClick = {},
                onWithdrawalClick = {},
                uiState = WithdrawalUiState(),
            )
        }
    }
}
