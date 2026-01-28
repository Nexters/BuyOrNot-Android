package com.sseotdabwa.buyornot.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

enum class BuyOrNotButtonType {
    Primary,
    Secondary,
}

@Composable
fun BuyOrNotButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: BuyOrNotButtonType = BuyOrNotButtonType.Primary, // 기본값 설정
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val height =
        when (type) {
            BuyOrNotButtonType.Primary -> 50.dp
            BuyOrNotButtonType.Secondary -> 40.dp
        }

    val horizontalPadding =
        when (type) {
            BuyOrNotButtonType.Primary -> 16.dp
            BuyOrNotButtonType.Secondary -> 12.dp
        }

    val radius =
        when (type) {
            BuyOrNotButtonType.Primary -> 14.dp
            BuyOrNotButtonType.Secondary -> 10.dp
        }

    val buttonColors =
        when (type) {
            BuyOrNotButtonType.Primary ->
                ButtonDefaults.buttonColors(
                    containerColor = BuyOrNotTheme.colors.gray900,
                    contentColor = BuyOrNotTheme.colors.gray0,
                    disabledContainerColor = BuyOrNotTheme.colors.gray400,
                    disabledContentColor = BuyOrNotTheme.colors.gray0,
                )
            BuyOrNotButtonType.Secondary ->
                ButtonDefaults.buttonColors(
                    containerColor = BuyOrNotTheme.colors.gray100,
                    contentColor = BuyOrNotTheme.colors.gray700,
                    disabledContainerColor = BuyOrNotTheme.colors.gray100,
                    disabledContentColor = BuyOrNotTheme.colors.gray700,
                )
        }

    Button(
        onClick = onClick,
        modifier =
            modifier
                .height(height),
        enabled = enabled,
        shape = RoundedCornerShape(radius),
        colors = buttonColors,
        contentPadding = PaddingValues(horizontal = horizontalPadding),
    ) {
        content()
    }
}

@Preview
@Composable
fun BuyOrNotButtonPreview() {
    BuyOrNotTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row {
                BuyOrNotButton(
                    modifier = Modifier.weight(1f),
                    onClick = { /* 클릭 동작 */ },
                ) {
                    Text(
                        text = "Button",
                        style = BuyOrNotTheme.typography.titleT2Bold,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                BuyOrNotButton(
                    modifier = Modifier.weight(1f),
                    onClick = { /* 클릭 동작 */ },
                    enabled = false,
                ) {
                    Text(
                        text = "Button",
                        style = BuyOrNotTheme.typography.titleT2Bold,
                    )
                }
            }

            BuyOrNotButton(
                onClick = { },
                type = BuyOrNotButtonType.Secondary,
            ) {
                Text(
                    text = "Button",
                    style = BuyOrNotTheme.typography.subTitleS5SemiBold,
                )
            }
        }
    }
}
