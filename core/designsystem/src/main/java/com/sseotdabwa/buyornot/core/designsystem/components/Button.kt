package com.sseotdabwa.buyornot.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

/**
 * 내부 공통 Button 컴포넌트
 */
@Composable
private fun BaseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp,
    cornerRadius: Dp,
    colors: ButtonColors,
    contentPadding: PaddingValues,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        enabled = enabled,
        shape = RoundedCornerShape(cornerRadius),
        colors = colors,
        contentPadding = contentPadding,
        content = content,
    )
}

/**
 * Primary Button - 주요 액션에 사용
 *
 * @param onClick 클릭 이벤트 핸들러
 * @param modifier Modifier
 * @param enabled 활성화 여부
 * @param cornerRadius 버튼 모서리 둥글기
 * @param content 버튼 내부 컨텐츠
 */
@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cornerRadius: Dp = 14.dp,
    content: @Composable RowScope.() -> Unit,
) {
    BaseButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        height = 50.dp,
        cornerRadius = cornerRadius,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = BuyOrNotTheme.colors.gray900,
                contentColor = BuyOrNotTheme.colors.gray0,
                disabledContainerColor = BuyOrNotTheme.colors.gray400,
                disabledContentColor = BuyOrNotTheme.colors.gray700,
            ),
        contentPadding = PaddingValues(horizontal = 16.dp),
        content = content,
    )
}

/**
 * Secondary Button - 보조 액션에 사용 (Filled 스타일)
 *
 * @param onClick 클릭 이벤트 핸들러
 * @param modifier Modifier
 * @param enabled 활성화 여부
 * @param cornerRadius 버튼 모서리 둥글기
 * @param content 버튼 내부 컨텐츠
 */
@Composable
fun SecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cornerRadius: Dp = 10.dp,
    content: @Composable RowScope.() -> Unit,
) {
    BaseButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        height = 40.dp,
        cornerRadius = cornerRadius,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = BuyOrNotTheme.colors.gray100,
                contentColor = BuyOrNotTheme.colors.gray700,
                disabledContainerColor = BuyOrNotTheme.colors.gray100,
                disabledContentColor = BuyOrNotTheme.colors.gray700,
            ),
        contentPadding = PaddingValues(horizontal = 12.dp),
        content = content,
    )
}

/**
 * Secondary Outlined Button - 보조 액션에 사용 (Outlined 스타일)
 *
 * @param onClick 클릭 이벤트 핸들러
 * @param modifier Modifier
 * @param enabled 활성화 여부
 * @param cornerRadius 버튼 모서리 둥글기
 * @param content 버튼 내부 컨텐츠
 */
@Composable
fun SecondaryOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cornerRadius: Dp = 10.dp,
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        enabled = enabled,
        shape = RoundedCornerShape(cornerRadius),
        colors =
            ButtonDefaults.outlinedButtonColors(
                contentColor = BuyOrNotTheme.colors.gray800,
                disabledContentColor = BuyOrNotTheme.colors.gray700,
            ),
        border =
            BorderStroke(
                width = 1.dp,
                color = if (enabled) BuyOrNotTheme.colors.gray300 else BuyOrNotTheme.colors.gray200,
            ),
        contentPadding = PaddingValues(horizontal = 12.dp),
    ) {
        content()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ButtonPreview() {
    BuyOrNotTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Primary Button
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PrimaryButton(
                    modifier = Modifier.weight(1f),
                    onClick = { },
                ) {
                    Text(
                        text = "Primary",
                        style = BuyOrNotTheme.typography.titleT2Bold,
                    )
                }

                PrimaryButton(
                    modifier = Modifier.weight(1f),
                    onClick = { },
                    enabled = false,
                ) {
                    Text(
                        text = "Primary",
                        style = BuyOrNotTheme.typography.titleT2Bold,
                    )
                }
            }

            // Secondary Button (Filled)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SecondaryButton(
                    onClick = { },
                ) {
                    Text(
                        text = "Secondary",
                        style = BuyOrNotTheme.typography.subTitleS5SemiBold,
                    )
                }

                SecondaryOutlinedButton(
                    onClick = { },
                ) {
                    Text(
                        text = "Outlined",
                        style = BuyOrNotTheme.typography.subTitleS5SemiBold,
                    )
                }

                SecondaryButton(
                    onClick = { },
                    cornerRadius = 100.dp,
                ) {
                    Text(
                        text = "Secondary",
                        style = BuyOrNotTheme.typography.subTitleS5SemiBold,
                    )
                }
            }
        }
    }
}
