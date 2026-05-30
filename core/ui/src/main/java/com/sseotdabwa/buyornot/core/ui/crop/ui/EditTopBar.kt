package com.sseotdabwa.buyornot.core.ui.crop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.core.ui.crop.state.EditMode

@Composable
internal fun EditTopBar(
    mode: EditMode,
    isConfirmEnabled: Boolean,
    onLeftAction: () -> Unit,
    onConfirm: () -> Unit,
) {
    val leftIcon =
        when (mode) {
            EditMode.Idle -> BuyOrNotIcons.ArrowLeft
            EditMode.Crop, EditMode.Rotate -> BuyOrNotIcons.Close
        }
    val leftContentDescription =
        when (mode) {
            EditMode.Idle -> "취소"
            EditMode.Crop -> "자르기 취소"
            EditMode.Rotate -> "회전 취소"
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .height(60.dp)
                .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onLeftAction) {
            Icon(
                imageVector = leftIcon.asImageVector(),
                contentDescription = leftContentDescription,
                tint = BuyOrNotTheme.colors.gray0,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onConfirm, enabled = isConfirmEnabled) {
            Text(
                text = "완료",
                style = BuyOrNotTheme.typography.subTitleS2SemiBold,
                color = BuyOrNotTheme.colors.gray0,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun EditTopBarIdlePreview() {
    EditTopBar(
        mode = EditMode.Idle,
        isConfirmEnabled = true,
        onLeftAction = {},
        onConfirm = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun EditTopBarCropPreview() {
    EditTopBar(
        mode = EditMode.Crop,
        isConfirmEnabled = true,
        onLeftAction = {},
        onConfirm = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun EditTopBarRotatePreview() {
    EditTopBar(
        mode = EditMode.Rotate,
        isConfirmEnabled = false,
        onLeftAction = {},
        onConfirm = {},
    )
}
