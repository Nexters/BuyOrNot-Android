package com.sseotdabwa.buyornot.feature.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotImgs
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

@Composable
fun HomeBanner(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
        Box(
            modifier = Modifier
                .width(335.dp)
                .height(173.dp)
                //.aspectRatio(335f / 173f) // 배너 비율 예시
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(start = 18.dp, bottom = 16.dp, end = 16.dp) // 가이드의 좌우 여백 반영
                .clickable { onClick() }
        ) {
                // 닫기 버튼 (터치 영역 24x24 반영)
                IconButton(
                    onClick = onClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 18.dp)
                        .size(24.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismiss
                        ),
                ) {
                    Icon(
                        imageVector = BuyOrNotIcons.Close.asImageVector(),
                        contentDescription = "닫기",
                        tint = BuyOrNotTheme.colors.gray500,
                        modifier = Modifier.size(24.dp)
                            .padding(4.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = BuyOrNotImgs.HomeBanner.resId),
                        contentDescription = null,
                        modifier = Modifier
                            .size(width =204.dp, height = 113.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF1A1C20), Color(0xFF8A97B2)),
                                    radius = 800f
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "고민되는 소비가 있나요?",
                            style = BuyOrNotTheme.typography.subTitleS4SemiBold,
                            color = BuyOrNotTheme.colors.gray0 // 메인 텍스트 Gray 0 반영
                        )
                    }

                // 하단 버튼/텍스트 영역

            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BuyOrNotTheme.colors.gray300) // Card Stroke Gray 300 반영
            )
        }
}

@Preview(name = "SplashScreen - Pixel 5", device = "id:pixel_5", showBackground = true)
@Composable
private fun HomeBannerPreview() {
    BuyOrNotTheme {
        HomeBanner(onDismiss = {}, onClick = {})
    }
}
