package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

/**
 * 알림 화면
 * 사용자의 알림 목록을 표시하는 화면
 */
@Composable
fun NotificationScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { NotificationTopBar() },
    ) { innerPadding ->
        NotificationContent(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        )
    }
}

/**
 * 알림 화면 상단 바
 */
@Composable
private fun NotificationTopBar() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(BuyOrNotTheme.colors.gray0)
                .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            text = "알림",
            style = BuyOrNotTheme.typography.headingH3Bold,
            color = BuyOrNotTheme.colors.gray900,
            modifier = Modifier.align(Alignment.CenterStart),
        )
    }
}

/**
 * 알림 목록 콘텐츠
 */
@Composable
private fun NotificationContent(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(BuyOrNotTheme.colors.gray0),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "알림이 없습니다",
                style = BuyOrNotTheme.typography.bodyB2Medium,
                color = BuyOrNotTheme.colors.gray500,
            )
        }
    }
}

@Preview(name = "NotificationScreen", showBackground = true)
@Composable
private fun NotificationScreenPreview() {
    BuyOrNotTheme {
        NotificationScreen()
    }
}
