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
fun BuyOrNotAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    subText: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmButtonColors: BuyOrNotButtonColors = BuyOrNotButtonDefaults.primaryButtonColors(),
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Surface(
            modifier =
                modifier
                    .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp), // 이미지와 유사한 둥근 모서리
            color = BuyOrNotTheme.colors.gray0,
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(horizontal = 18.dp)
                        .padding(top = 26.dp, bottom = 16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 6.dp),
                ) {
                    Text(
                        text = title,
                        style = BuyOrNotTheme.typography.titleT2Bold,
                        color = BuyOrNotTheme.colors.gray900,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = subText,
                        style = BuyOrNotTheme.typography.bodyB3Medium,
                        color = BuyOrNotTheme.colors.gray700,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                        colors = confirmButtonColors,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BuyOrNotDestructiveAlertDialogPreview() {
    BuyOrNotTheme {
        Box(
            modifier =
                Modifier
                    .background(BuyOrNotTheme.colors.gray1000)
                    .padding(10.dp),
        ) {
            BuyOrNotAlertDialog(
                onDismissRequest = { /* Handle dismiss */ },
                title = "피드를 삭제할까요?",
                subText = "삭제된 피드는 복구할 수 없어요.",
                confirmText = "삭제",
                dismissText = "취소",
                onConfirm = { /* Handle confirm */ },
                onDismiss = { /* Handle dismiss */ },
                confirmButtonColors = BuyOrNotButtonDefaults.destructiveButtonColors(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BuyOrNotAlertDialogPreview() {
    BuyOrNotTheme {
        Box(
            modifier =
                Modifier
                    .background(BuyOrNotTheme.colors.gray1000)
                    .padding(10.dp),
        ) {
            BuyOrNotAlertDialog(
                onDismissRequest = { /* Handle dismiss */ },
                title = "제목입니다",
                subText = "자세한 내용이 입력됩니다. 자세한 내용이 입력됩니다. 자세한 내용이 입력됩니다.",
                confirmText = "확인",
                dismissText = "취소",
                onConfirm = { /* Handle confirm */ },
                onDismiss = { /* Handle dismiss */ },
            )
        }
    }
}
