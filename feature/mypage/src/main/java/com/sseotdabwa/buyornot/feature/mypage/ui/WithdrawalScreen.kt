package com.sseotdabwa.buyornot.feature.mypage.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.components.BackTopBarWithTitle
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotConfirmDialog
import com.sseotdabwa.buyornot.core.designsystem.components.PrimaryButton
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotImgs
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

@Composable
fun WithdrawalRoute(onBackClick: () -> Unit) {
    WithdrawalScreen(onBackClick = onBackClick)
}

@Composable
fun WithdrawalScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
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
                "userName님,\n살까말까를 떠나시나요?",
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
            onDismiss = { isWithdrawalDialogVisible = false },
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
            )
        }
    }
}
