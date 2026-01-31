package com.sseotdabwa.buyornot.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

private object ClickableIconDefaults {
    val IconSize = 20.dp
    val TouchTargetSize = 40.dp
    val RippleRadius = 24.dp
}

/**
 * 클릭 가능한 아이콘 컴포넌트
 *
 * 최소 터치 영역(40dp)을 제공하면서 아이콘 크기(20dp)는 작게 유지합니다.
 * Ripple 효과와 접근성 지원이 포함되어 있습니다.
 *
 * @param imageVector 표시할 아이콘
 * @param contentDescription 접근성을 위한 설명
 * @param onClick 클릭 시 실행될 콜백
 * @param modifier 추가 modifier
 * @param tint 아이콘 색상. 기본값은 현재 contentColor
 * @param iconSize 아이콘 크기. 기본값은 20.dp
 * @param touchTargetSize 터치 영역 크기. 기본값은 40.dp
 * @param alignment 아이콘 정렬 위치. 기본값은 Center
 */
@Composable
fun ClickableIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    iconSize: Dp = ClickableIconDefaults.IconSize,
    touchTargetSize: Dp = ClickableIconDefaults.TouchTargetSize,
    alignment: Alignment = Alignment.Center,
) {
    Box(
        modifier =
            modifier
                .size(touchTargetSize)
                .clickable(
                    onClick = onClick,
                    role = Role.Button,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false, radius = ClickableIconDefaults.RippleRadius),
                ),
        contentAlignment = alignment,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = tint,
        )
    }
}

@Preview(name = "ClickableIcon - Default", showBackground = true)
@Composable
private fun ClickableIconPreview() {
    BuyOrNotTheme {
        ClickableIcon(
            imageVector = BuyOrNotIcons.Profile.asImageVector(),
            contentDescription = "Profile",
            onClick = {},
        )
    }
}

@Preview(name = "ClickableIcon - With Tint", showBackground = true)
@Composable
private fun ClickableIconWithTintPreview() {
    BuyOrNotTheme {
        ClickableIcon(
            imageVector = BuyOrNotIcons.NotificationFilled.asImageVector(),
            contentDescription = "Notification",
            onClick = {},
            tint = BuyOrNotTheme.colors.gray500,
        )
    }
}

@Preview(name = "ClickableIcon - CenterStart Alignment", showBackground = true)
@Composable
private fun ClickableIconCenterStartPreview() {
    BuyOrNotTheme {
        ClickableIcon(
            imageVector = BuyOrNotIcons.ArrowLeft.asImageVector(),
            contentDescription = "Back",
            onClick = {},
            alignment = Alignment.CenterStart,
        )
    }
}
