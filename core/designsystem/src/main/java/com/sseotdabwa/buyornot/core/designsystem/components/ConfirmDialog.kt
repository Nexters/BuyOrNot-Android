package com.sseotdabwa.buyornot.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

@Composable
fun BuyOrNotConfirmDialog(
    onDismissRequest: () -> Unit,
    title: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = BuyOrNotTheme.colors.gray0,
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(horizontal = 18.dp)
                        .padding(top = 26.dp, bottom = 16.dp),
            ) {
                Text(
                    text = title,
                    modifier = Modifier.padding(horizontal = 6.dp),
                    style = BuyOrNotTheme.typography.titleT2Bold,
                    color = BuyOrNotTheme.colors.gray950,
                )

                Spacer(modifier = Modifier.height(26.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    NeutralButton(
                        text = dismissText,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    )

                    PrimaryButton(
                        text = confirmText,
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BuyOrNotConfirmDialogPreview() {
    BuyOrNotTheme {
        Box(
            modifier =
                Modifier
                    .background(BuyOrNotTheme.colors.gray1000)
                    .padding(10.dp),
        ) {
            BuyOrNotConfirmDialog(
                onDismissRequest = { },
                title = "로그아웃 하시겠어요?",
                confirmText = "유지하기",
                dismissText = "로그아웃",
                onConfirm = { },
                onDismiss = { },
            )
        }
    }
}
