package com.sseotdabwa.buyornot.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyOrNotChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 상태 변화를 감지하기 위한 interactionSource
    val interactionSource = remember { MutableInteractionSource() }

    // Hover 및 Pressed 상태 수집
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    // 상태별 색상 결정 로직
    val backgroundColor by animateColorAsState(
        targetValue =
            when {
                isSelected -> BuyOrNotTheme.colors.gray950 // Selected
                isPressed || isHovered -> BuyOrNotTheme.colors.gray300 // Hover/Pressed
                else -> BuyOrNotTheme.colors.gray0 // Unselected
            },
        label = "backgroundColor",
    )

    val contentColor by animateColorAsState(
        targetValue =
            when {
                isSelected -> BuyOrNotTheme.colors.gray0
                else -> BuyOrNotTheme.colors.gray950
            },
        label = "contentColor",
    )

    val textStyle =
        if (isSelected) {
            BuyOrNotTheme.typography.subTitleS5SemiBold
        } else {
            BuyOrNotTheme.typography.bodyB5Medium
        }

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color.Transparent else BuyOrNotTheme.colors.gray300,
        label = "borderColor",
    )

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        Surface(
            modifier = modifier,
            onClick = onClick,
            shape = CircleShape,
            color = backgroundColor,
            contentColor = contentColor,
            border = BorderStroke(width = 1.dp, color = borderColor),
            interactionSource = interactionSource,
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = text,
                    style = textStyle,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyOrNotIconChip(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = BuyOrNotTheme.colors.gray950,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        Surface(
            modifier = modifier,
            onClick = onClick,
            shape = CircleShape,
            color = BuyOrNotTheme.colors.gray0,
            border = BorderStroke(width = 1.dp, color = BuyOrNotTheme.colors.gray300),
            interactionSource = interactionSource,
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = contentDescription,
                    tint = tint,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Preview(name = "Chip Preview")
@Composable
fun BuyOrNotChipPreview() {
    BuyOrNotTheme {
        var isSelected by remember { mutableStateOf(false) }

        BuyOrNotChip(
            text = "Selected Chip",
            isSelected = isSelected,
            onClick = { isSelected = !isSelected },
        )
    }
}

@Preview(name = "IconChip Preview")
@Composable
fun BuyOrNotIconChipPreview() {
    BuyOrNotTheme {
        BuyOrNotIconChip(
            imageVector = BuyOrNotIcons.Sort.asImageVector(),
            contentDescription = "정렬 칩",
            onClick = {},
        )
    }
}
