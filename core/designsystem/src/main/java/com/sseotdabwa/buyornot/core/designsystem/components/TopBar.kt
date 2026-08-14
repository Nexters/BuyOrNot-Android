package com.sseotdabwa.buyornot.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.R
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

private object TopBarDefaults {
    val Height = 60.dp
    val StartPadding = 10.dp
    val EndPadding = 20.dp
    val IconSpacing = 4.dp
}

@Composable
private fun BaseTopBar(
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
) {
    Box(
        modifier =
            modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
                .height(TopBarDefaults.Height)
                .background(BuyOrNotTheme.colors.gray0)
                .padding(start = TopBarDefaults.StartPadding, end = TopBarDefaults.EndPadding),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // 1. 왼쪽 영역 (뒤로가기 또는 로고)
            navigationIcon?.invoke()

            // 2. 중앙 영역 (타이틀)
            title?.invoke()
        }

        // 3. 오른쪽 영역 (아이콘들 또는 버튼)
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions?.invoke(this)
        }
    }
}

/**
 * 1. 뒤로가기 전용 TopBar
 */
@Composable
fun BackTopBar(onBackClick: () -> Unit) {
    BaseTopBar(
        navigationIcon = {
            ClickableIcon(
                imageVector = BuyOrNotIcons.ArrowLeft.asImageVector(),
                contentDescription = "Back",
                onClick = onBackClick,
            )
        },
    )
}

/**
 * 2. 뒤로가기 + 타이틀 전용 TopBar
 */
@Composable
fun BackTopBarWithTitle(
    title: String,
    onBackClick: () -> Unit,
) {
    BaseTopBar(
        navigationIcon = {
            ClickableIcon(
                imageVector = BuyOrNotIcons.ArrowLeft.asImageVector(),
                contentDescription = "Back",
                onClick = onBackClick,
            )
        },
        title = {
            Text(
                text = title,
                style = BuyOrNotTheme.typography.titleT1Bold,
                color = BuyOrNotTheme.colors.gray950,
            )
        },
    )
}

/**
 * 3. 뒤로가기 + 완료 버튼 TopBar (back-done)
 */
@Composable
fun BackTopBarWithDone(
    onBackClick: () -> Unit,
    onDoneClick: () -> Unit,
) {
    BaseTopBar(
        navigationIcon = {
            ClickableIcon(
                imageVector = BuyOrNotIcons.ArrowLeft.asImageVector(),
                contentDescription = "Back",
                onClick = onBackClick,
            )
        },
        actions = {
            TextButton(
                onClick = onDoneClick,
                colors =
                    ButtonDefaults.textButtonColors(
                        contentColor = BuyOrNotTheme.colors.gray950,
                    ),
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(
                    text = "완료",
                    style = BuyOrNotTheme.typography.subTitleS2SemiBold,
                )
            }
        },
    )
}

/**
 * 4. 홈 화면용 TopBar (로고 + 알림 + 프로필)
 */
@Composable
fun HomeTopBar(
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    unreadCount: Int = 0,
) {
    BaseTopBar(
        modifier = modifier,
        navigationIcon = {
            Icon(
                imageVector = BuyOrNotIcons.AppLogo.asImageVector(),
                contentDescription = "App Logo",
                modifier = Modifier.padding(start = 12.dp),
            )
        },
        actions = {
            // ClickableIcon이 유일한 클릭 대상(40dp 터치 영역)이며, 배지는 그 위에 얹힌
            // 비상호작용 시각 오버레이입니다. 배지에는 pointerInput/clickable이 없어 탭을 소비하지 않습니다.
            Box {
                ClickableIcon(
                    imageVector = BuyOrNotIcons.NotificationFilled.asImageVector(),
                    contentDescription = "Notification",
                    onClick = onNotificationClick,
                    tint = BuyOrNotTheme.colors.gray600,
                    alignment = Alignment.CenterEnd,
                )

                if (unreadCount > 0) {
                    // Figma: 40dp 터치 영역의 우상단 모서리 기준으로 오른쪽 위로 살짝 튀어나오게 배치.
                    // (badge 우상단 = 터치영역 우상단 + (x +6, y +3))
                    NotificationNumberBadge(
                        count = unreadCount,
                        modifier =
                            Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 6.dp, y = 3.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(TopBarDefaults.IconSpacing))

            ClickableIcon(
                imageVector = BuyOrNotIcons.Profile.asImageVector(),
                contentDescription = "Profile",
                onClick = onProfileClick,
                tint = BuyOrNotTheme.colors.gray600,
                alignment = Alignment.CenterEnd,
            )
        },
    )
}

/**
 * 5. 게스트/로그인 유도용 TopBar (로고 + 로그인 버튼)
 */
@Composable
fun GuestTopBar(onLoginClick: () -> Unit) {
    BaseTopBar(
        navigationIcon = {
            Icon(
                imageVector = BuyOrNotIcons.AppLogo.asImageVector(),
                contentDescription = "App Logo",
                modifier = Modifier.padding(start = 12.dp),
            )
        },
        actions = {
            TextButton(
                onClick = onLoginClick,
                shape = RoundedCornerShape(10.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = BuyOrNotTheme.colors.gray0,
                        contentColor = BuyOrNotTheme.colors.gray800,
                    ),
                border =
                    BorderStroke(
                        width = 1.dp,
                        color = BuyOrNotTheme.colors.gray300,
                    ),
                contentPadding =
                    PaddingValues(
                        horizontal = 12.dp,
                        vertical = 12.dp,
                    ),
            ) {
                Text(
                    stringResource(R.string.login_signup),
                    style = BuyOrNotTheme.typography.subTitleS5SemiBold,
                )
            }
        },
    )
}

@Preview(name = "BackTopBar", showBackground = true)
@Composable
private fun BackTopBarWithoutTitlePreview() {
    BuyOrNotTheme {
        BackTopBar(
            onBackClick = {},
        )
    }
}

@Preview(name = "BackTopBarWithTitle", showBackground = true)
@Composable
private fun BackTopBarWithTitlePreview() {
    BuyOrNotTheme {
        BackTopBarWithTitle(
            title = "투표 피드",
            onBackClick = {},
        )
    }
}

@Preview(name = "BackTopBarWithDone", showBackground = true)
@Composable
private fun BackTopBarWithDonePreview() {
    BuyOrNotTheme {
        BackTopBarWithDone(
            onBackClick = {},
            onDoneClick = {},
        )
    }
}

@Preview(name = "HomeTopBar", showBackground = true)
@Composable
private fun HomeTopBarPreview() {
    BuyOrNotTheme {
        HomeTopBar(
            onNotificationClick = {},
            onProfileClick = {},
            unreadCount = 3,
        )
    }
}

@Preview(name = "GuestTopBar", showBackground = true)
@Composable
private fun GuestTopBarPreview() {
    BuyOrNotTheme {
        Box(
            modifier = Modifier.background(BuyOrNotTheme.colors.gray0),
        ) {
            GuestTopBar(
                onLoginClick = {},
            )
        }
    }
}
