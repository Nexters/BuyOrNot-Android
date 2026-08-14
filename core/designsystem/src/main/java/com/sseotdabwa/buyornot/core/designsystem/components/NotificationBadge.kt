package com.sseotdabwa.buyornot.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

private object NotificationBadgeDefaults {
    val NumberMinWidth = 18.dp
    val NumberHorizontalPadding = 4.dp
    val NumberVerticalPadding = 2.dp
    val DotSize = 5.dp
    const val MAX_COUNT = 99
}

/**
 * 안 읽은 알림 수를 표시하는 number 배지.
 *
 * 배경 red100(#FF3830), 흰색 Bold 10 텍스트, pill 형태(완전 라운드), 최소 너비 18dp,
 * 자릿수가 늘어나면 우측으로 확장됩니다. count가 99를 초과하면 "99+"로 표시합니다.
 *
 * count == 0인 경우 호출부에서 노출 여부를 결정합니다(이 컴포저블은 값과 무관하게 항상 렌더링).
 *
 * @param count 표시할 안 읽은 알림 수 (호출부에서 count > 0 일 때만 노출 권장)
 */
@Composable
fun NotificationNumberBadge(
    count: Int,
    modifier: Modifier = Modifier,
) {
    val text = if (count > NotificationBadgeDefaults.MAX_COUNT) "${NotificationBadgeDefaults.MAX_COUNT}+" else count.toString()
    Box(
        modifier =
            modifier
                .defaultMinSize(minWidth = NotificationBadgeDefaults.NumberMinWidth)
                .clip(CircleShape)
                .background(BuyOrNotTheme.colors.red100)
                .padding(
                    horizontal = NotificationBadgeDefaults.NumberHorizontalPadding,
                    vertical = NotificationBadgeDefaults.NumberVerticalPadding,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = BuyOrNotTheme.typography.titleT7Bold,
            color = BuyOrNotTheme.colors.gray0,
        )
    }
}

/**
 * 5dp red 원형 점 배지.
 */
@Composable
fun NotificationDotBadge(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .size(NotificationBadgeDefaults.DotSize)
                .clip(CircleShape)
                .background(BuyOrNotTheme.colors.red100),
    )
}

@Preview(name = "NotificationNumberBadge - 1", showBackground = true)
@Composable
private fun NotificationNumberBadgeSinglePreview() {
    BuyOrNotTheme {
        NotificationNumberBadge(count = 1)
    }
}

@Preview(name = "NotificationNumberBadge - 99", showBackground = true)
@Composable
private fun NotificationNumberBadgeMaxPreview() {
    BuyOrNotTheme {
        NotificationNumberBadge(count = 99)
    }
}

@Preview(name = "NotificationNumberBadge - 100 (99+)", showBackground = true)
@Composable
private fun NotificationNumberBadgeOverflowPreview() {
    BuyOrNotTheme {
        NotificationNumberBadge(count = 100)
    }
}

@Preview(name = "NotificationDotBadge", showBackground = true)
@Composable
private fun NotificationDotBadgePreview() {
    BuyOrNotTheme {
        NotificationDotBadge()
    }
}
