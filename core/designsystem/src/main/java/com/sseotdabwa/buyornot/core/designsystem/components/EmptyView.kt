package com.sseotdabwa.buyornot.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotImgs
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

/**
 * 공통으로 사용할 수 있는 빈 상태 뷰 컴포넌트
 *
 * @param modifier 컴포넌트에 적용할 Modifier
 * @param title 메인 타이틀 텍스트
 * @param description 서브 설명 문구
 * @param image 표시할 이미지 리소스
 */
@Composable
fun BuyOrNotEmptyView(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    @DrawableRes image: Int,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = image),
            contentDescription = null,
            modifier = Modifier.size(140.dp),
            contentScale = ContentScale.Fit,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = BuyOrNotTheme.typography.titleT1Bold,
            color = BuyOrNotTheme.colors.gray800,
        )

        Text(
            modifier = Modifier.padding(top = 6.dp),
            text = description,
            style = BuyOrNotTheme.typography.bodyB5Medium,
            color = BuyOrNotTheme.colors.gray600,
        )
    }
}

@Preview(name = "BuyOrNotEmptyView")
@Composable
private fun BuyOrNotEmptyViewPreview() {
    BuyOrNotTheme {
        BuyOrNotEmptyView(
            title = "새로운 알림이 없어요",
            description = "투표에 참여하고 소식을 받아보세요!",
            image = BuyOrNotImgs.NoNotification.resId,
        )
    }
}
