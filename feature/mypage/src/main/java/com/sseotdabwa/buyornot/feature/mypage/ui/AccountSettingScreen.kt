package com.sseotdabwa.buyornot.feature.mypage.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.components.BackTopBarWithTitle
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotConfirmDialog
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.feature.mypage.components.SettingItem

@Composable
fun AccountSettingRoute(
    onBackClick: () -> Unit,
    onWithdrawalClick: () -> Unit,
) {
    AccountSettingScreen(
        onBackClick = onBackClick,
        onWithdrawalClick = onWithdrawalClick,
    )
}

@Composable
fun AccountSettingScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onWithdrawalClick: () -> Unit,
) {
    var isLogoutDialogVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        BackTopBarWithTitle(
            title = "계정 설정",
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier.padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EmailItem("buyornot@gmail.com")
            SettingItem("로그아웃") { isLogoutDialogVisible = true }
            SettingItem(
                title = "회원 탈퇴",
                textColor = BuyOrNotTheme.colors.red100,
            ) {
                onWithdrawalClick()
            }
        }
    }

    if (isLogoutDialogVisible) {
        BuyOrNotConfirmDialog(
            onDismissRequest = { isLogoutDialogVisible = false },
            title = "로그아웃 하시겠어요?",
            confirmText = "유지하기",
            dismissText = "로그아웃",
            onConfirm = { isLogoutDialogVisible = false },
            onDismiss = { isLogoutDialogVisible = false },
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
            color = BuyOrNotTheme.colors.gray900,
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
                onWithdrawalClick = {},
            )
        }
    }
}
