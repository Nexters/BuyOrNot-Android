package com.sseotdabwa.buyornot.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

data class BuyOrNotButtonColors(
    val defaultContainer: Color,
    val hoverContainer: Color,
    val pressedContainer: Color,
    val disabledContainer: Color,
    val content: Color,
    val disabledContent: Color,
)

object BuyOrNotButtonDefaults {
    @Composable
    fun primaryButtonColors() =
        BuyOrNotButtonColors(
            defaultContainer = BuyOrNotTheme.colors.gray900,
            hoverContainer = BuyOrNotTheme.colors.gray800,
            pressedContainer = BuyOrNotTheme.colors.gray1000,
            disabledContainer = BuyOrNotTheme.colors.gray300,
            content = BuyOrNotTheme.colors.gray0,
            disabledContent = BuyOrNotTheme.colors.gray700,
        )

    @Composable
    fun secondaryButtonColors() =
        BuyOrNotButtonColors(
            defaultContainer = BuyOrNotTheme.colors.gray100,
            hoverContainer = BuyOrNotTheme.colors.gray300,
            pressedContainer = BuyOrNotTheme.colors.gray400,
            disabledContainer = BuyOrNotTheme.colors.gray100,
            content = BuyOrNotTheme.colors.gray700,
            disabledContent = BuyOrNotTheme.colors.gray500,
        )

    @Composable
    fun secondaryOutlinedButtonColors() =
        BuyOrNotButtonColors(
            defaultContainer = BuyOrNotTheme.colors.gray0,
            hoverContainer = BuyOrNotTheme.colors.gray100,
            pressedContainer = BuyOrNotTheme.colors.gray300,
            disabledContainer = BuyOrNotTheme.colors.gray0,
            content = BuyOrNotTheme.colors.gray800,
            disabledContent = BuyOrNotTheme.colors.gray500,
        )
}

@Composable
private fun BaseBuyOrNotButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp,
    shape: Shape,
    buttonColors: BuyOrNotButtonColors,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit,
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val containerColor by animateColorAsState(
        targetValue =
            when {
                !enabled -> buttonColors.disabledContainer
                isPressed -> buttonColors.pressedContainer
                isHovered -> buttonColors.hoverContainer
                else -> buttonColors.defaultContainer
            },
        label = "buttonContainerColor",
    )

    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        enabled = enabled,
        interactionSource = interactionSource,
        shape = shape,
        border = border,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = if (enabled) buttonColors.content else buttonColors.disabledContent,
                disabledContainerColor = buttonColors.disabledContainer, // ButtonDefaults에서 disabledContainerColor도 설정
                disabledContentColor = buttonColors.disabledContent,
            ),
        contentPadding = contentPadding,
        content = content,
    )
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    BaseBuyOrNotButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        height = 50.dp,
        shape = RoundedCornerShape(14.dp),
        buttonColors = BuyOrNotButtonDefaults.primaryButtonColors(),
        contentPadding = PaddingValues(horizontal = 24.dp), // 시안에 맞게 패딩 조절
        interactionSource = interactionSource,
    ) {
        Text(text = text, style = BuyOrNotTheme.typography.titleT2Bold) // 텍스트 스타일 적용
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    BaseBuyOrNotButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        height = 40.dp,
        shape = RoundedCornerShape(10.dp),
        buttonColors = BuyOrNotButtonDefaults.secondaryButtonColors(),
        contentPadding = PaddingValues(horizontal = 12.dp),
        interactionSource = interactionSource,
    ) {
        Text(text = text, style = BuyOrNotTheme.typography.subTitleS5SemiBold)
    }
}

@Composable
fun SecondaryOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    BaseBuyOrNotButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        height = 40.dp,
        shape = RoundedCornerShape(10.dp),
        buttonColors = BuyOrNotButtonDefaults.secondaryOutlinedButtonColors(),
        border =
            BorderStroke(
                width = 1.dp,
                color = if (enabled) BuyOrNotTheme.colors.gray300 else BuyOrNotTheme.colors.gray200,
            ),
        contentPadding = PaddingValues(horizontal = 12.dp),
        interactionSource = interactionSource,
    ) {
        Text(text = text, style = BuyOrNotTheme.typography.subTitleS5SemiBold)
    }
}

@Composable
fun CapsuleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    BaseBuyOrNotButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        height = 40.dp,
        shape = RoundedCornerShape(100.dp),
        buttonColors = BuyOrNotButtonDefaults.primaryButtonColors(), // Primary 색상 사용 예시
        contentPadding = PaddingValues(horizontal = 16.dp),
        interactionSource = interactionSource,
    ) {
        Text(text = text, style = BuyOrNotTheme.typography.subTitleS5SemiBold)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun BuyOrNotButtonPreview() {
    BuyOrNotTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "Primary Button", style = BuyOrNotTheme.typography.subTitleS5SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PrimaryButton(
                    text = "Enabled",
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    enabled = true,
                )
                PrimaryButton(
                    text = "Disabled",
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    enabled = false,
                )
            }

            Text(text = "Secondary Button", style = BuyOrNotTheme.typography.subTitleS5SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SecondaryButton(
                    text = "Enabled",
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    enabled = true,
                )
                SecondaryButton(
                    text = "Disabled",
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    enabled = false,
                )
            }

            Text(text = "Secondary Outlined Button", style = BuyOrNotTheme.typography.subTitleS5SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SecondaryOutlinedButton(
                    text = "Enabled",
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    enabled = true,
                )
                SecondaryOutlinedButton(
                    text = "Disabled",
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    enabled = false,
                )
            }

            Text(text = "Capsule Button", style = BuyOrNotTheme.typography.subTitleS5SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CapsuleButton(
                    text = "Enabled",
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    enabled = true,
                )
                CapsuleButton(
                    text = "Disabled",
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    enabled = false,
                )
            }
        }
    }
}
