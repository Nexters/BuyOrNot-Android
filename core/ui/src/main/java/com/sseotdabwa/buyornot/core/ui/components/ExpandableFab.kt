package com.sseotdabwa.buyornot.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

data class FabOption(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
)

@Composable
fun ExpandableFloatingActionButton(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<FabOption>,
    modifier: Modifier = Modifier,
    mainBackgroundColor: Color = BuyOrNotTheme.colors.gray800,
    mainContentColor: Color = BuyOrNotTheme.colors.gray0,
    menuBackgroundColor: Color = BuyOrNotTheme.colors.gray100,
    menuContentColor: Color = BuyOrNotTheme.colors.gray800,
) {
    // + 아이콘 회전 애니메이션
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "rotation",
    )

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier,
    ) {
        // 메뉴 카드 (애니메이션 없이 바로 표시)
        if (expanded) {
            FabMenuCard(
                options = options,
                backgroundColor = menuBackgroundColor,
                contentColor = menuContentColor,
                onOptionClick = { option ->
                    option.onClick()
                    onExpandedChange(false)
                },
            )
        }

        // 메인 FAB (+ 아이콘)
        FloatingActionButton(
            onClick = { onExpandedChange(!expanded) },
            shape = CircleShape,
            containerColor = if (expanded) BuyOrNotTheme.colors.gray0 else mainBackgroundColor,
            contentColor = if (expanded) BuyOrNotTheme.colors.gray1000 else mainContentColor,
            modifier = Modifier.size(60.dp),
        ) {
            Icon(
                imageVector = BuyOrNotIcons.Add.asImageVector(),
                contentDescription = if (expanded) "Close" else "Open",
                modifier =
                    Modifier
                        .size(24.dp)
                        .rotate(rotationAngle),
            )
        }
    }
}

@Composable
private fun FabMenuCard(
    options: List<FabOption>,
    modifier: Modifier = Modifier,
    backgroundColor: Color = BuyOrNotTheme.colors.gray0,
    contentColor: Color = BuyOrNotTheme.colors.gray1000,
    onOptionClick: (FabOption) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        shadowElevation = 8.dp,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
        ) {
            options.forEach { option ->
                Row(
                    modifier =
                        Modifier
                            .clickable { onOptionClick(option) }
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = option.label,
                        tint = contentColor,
                        modifier = Modifier.size(15.dp),
                    )

                    Text(
                        text = option.label,
                        style = BuyOrNotTheme.typography.bodyB3Medium,
                        color = contentColor,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun ExpandableFABPreview() {
    var isExpanded by remember { mutableStateOf(false) }
    val options =
        listOf(
            FabOption(
                icon = BuyOrNotIcons.Vote.asImageVector(),
                label = "투표 등록",
                onClick = { },
            ),
            FabOption(
                icon = BuyOrNotIcons.Bag.asImageVector(),
                label = "상품 리뷰",
                onClick = { },
            ),
        )

    BuyOrNotTheme {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(BuyOrNotTheme.colors.gray100),
        ) {
            // Dim 레이어
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) {
                                isExpanded = false
                            },
                )
            }

            ExpandableFloatingActionButton(
                expanded = isExpanded,
                onExpandedChange = { isExpanded = it },
                options = options,
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
            )
        }
    }
}
