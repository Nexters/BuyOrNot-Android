package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

/**
 * 알림 설정 유도 배너 컴포넌트
 *
 * @param onActionClick '알림 켜기' 클릭 시 콜백
 * @param modifier Modifier
 */
@Composable
fun NotificationGuideBanner(
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BuyOrNotTheme.colors.gray0)
            .border(color = BuyOrNotTheme.colors.gray200, width = 1.dp, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 1.아이콘
        Icon(
            imageVector = BuyOrNotIcons.NotificationFilled.asImageVector(),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = BuyOrNotTheme.colors.gray600,
        )

        // 2. 안내 문구
        Text(
            text = "투표 종료 및 결과 소식을 받아보세요",
            style = BuyOrNotTheme.typography.bodyB5Medium,
            color = BuyOrNotTheme.colors.gray700,
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f)
        )

        // 3. 알림 켜기 버튼 (텍스트 버튼 형태)
        Text(
            text = "알림 켜기",
            style = BuyOrNotTheme.typography.subTitleS5SemiBold,
            color = BuyOrNotTheme.colors.gray800,
            modifier = Modifier
                .clickable { onActionClick() }
                .padding(start = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationGuideBannerPreview() {
    BuyOrNotTheme {
        Box(
            modifier = Modifier.background(
                color = BuyOrNotTheme.colors.gray700
            )
                .padding(20.dp)
        ) {
            NotificationGuideBanner(onActionClick = {})
        }
    }

}
