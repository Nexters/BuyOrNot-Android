package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.components.BackTopBarWithTitle
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotChip
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotImgs
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

/**
 * 에러 발생 시 표시되는 공통 에러 뷰 컴포넌트
 *
 * @param modifier Modifier
 * @param message 표시할 에러 메시지
 * @param onRefreshClick 새로고침 버튼 클릭 콜백
 */
@Composable
fun NotificationEmptyView(
    modifier: Modifier = Modifier,
    title: String = "새로운 알림이 없어요",
    description: String = "투표에 참여하고 소식을 받아보세요!",
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = BuyOrNotImgs.NoNotification.resId),
            contentDescription = null,
            modifier = Modifier.size(140.dp),
            contentScale = ContentScale.Fit,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 메인 타이틀
        Text(
            text = title,
            style = BuyOrNotTheme.typography.titleT1Bold,
            color = BuyOrNotTheme.colors.gray800,
        )

        // 서브 설명 문구
        Text(
            modifier = Modifier.padding(top = 6.dp),
            text = description,
            style = BuyOrNotTheme.typography.bodyB5Medium,
            color = BuyOrNotTheme.colors.gray600,
        )
    }
}

/**
 * 케이스 1: 상단 인라인 안내(배너)가 있는 경우 - 여백 120px
 */
@Preview(name = "Empty Case - With Guide", showBackground = true)
@Composable
private fun NotificationEmptyWithGuidePreview() {
    BuyOrNotTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        color = BuyOrNotTheme.colors.gray0,
                    ),
        ) {
            BackTopBarWithTitle(title = "알림", onBackClick = {})

            Spacer(modifier = Modifier.height(20.dp))

            BuyOrNotChip(
                text = "전체",
                isSelected = true,
                onClick = {},
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            NotificationGuideBanner(
                onActionClick = {},
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(modifier = Modifier.height(120.dp))

            NotificationEmptyView()
        }
    }
}

/**
 * 케이스 2: 상단 인라인 안내가 없는 경우 - 여백 140px
 */
@Preview(name = "Empty Case - Without Guide", showBackground = true)
@Composable
private fun NotificationEmptyWithoutGuidePreview() {
    BuyOrNotTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(color = BuyOrNotTheme.colors.gray0),
        ) {
            BackTopBarWithTitle(title = "알림", onBackClick = {})

            Spacer(modifier = Modifier.height(20.dp))

            BuyOrNotChip(
                text = "전체",
                isSelected = true,
                onClick = {},
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(modifier = Modifier.height(140.dp))

            NotificationEmptyView()
        }
    }
}
