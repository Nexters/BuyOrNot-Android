package com.sseotdabwa.buyornot.feature.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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

/**
 * 홈 화면 배너 컴포넌트의 크기 및 스타일 상수
 */
private object HomeBannerDefaults {
    val BannerWidth = 335.dp
    val BannerHeight = 173.dp
    val BannerCornerRadius = 20.dp
    val BannerPaddingStart = 18.dp
    val BannerPaddingEnd = 18.dp
    val BannerPaddingBottom = 16.dp

    val CloseButtonSize = 24.dp
    val CloseButtonPadding = 18.dp
    val CloseIconPadding = 4.dp

    val ImageWidth = 204.dp
    val ImageHeight = 113.dp

    val ActionButtonHeight = 44.dp
    val ActionButtonCornerRadius = 12.dp
    val ActionButtonGradientRadius = 600f

    val DividerHeight = 1.dp

    val GradientStartColor = Color(0xFF1A1C20)
    val GradientEndColor = Color(0xFF8A97B2)
}

/**
 * 홈 화면에 표시되는 배너 컴포넌트
 *
 * @param modifier 컴포넌트에 적용할 Modifier
 * @param onDismiss 닫기 버튼 클릭 시 호출되는 콜백
 * @param onClick 배너 클릭 시 호출되는 콜백
 */
@Composable
fun HomeBanner(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .width(HomeBannerDefaults.BannerWidth)
            .height(HomeBannerDefaults.BannerHeight)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(HomeBannerDefaults.BannerCornerRadius)
            )
            .border(
                width = 1.dp,
                color = BuyOrNotTheme.colors.gray300,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onClick() }
            .padding(
                start = HomeBannerDefaults.BannerPaddingStart,
                end = HomeBannerDefaults.BannerPaddingEnd,
                bottom = HomeBannerDefaults.BannerPaddingBottom
            )
    ) {
        HomeBannerCloseButton(
            onDismiss = onDismiss,
            modifier = Modifier.align(Alignment.TopEnd)
        )

        HomeBannerContent(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        )

        HomeBannerDivider(
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * 홈 배너의 닫기 버튼 컴포넌트
 *
 * @param onDismiss 버튼 클릭 시 호출되는 콜백
 * @param modifier 컴포넌트에 적용할 Modifier
 */
@Composable
private fun HomeBannerCloseButton(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onDismiss,
        modifier = modifier
            .padding(top = HomeBannerDefaults.CloseButtonPadding)
            .size(HomeBannerDefaults.CloseButtonSize)
    ) {
        Icon(
            imageVector = BuyOrNotIcons.Close.asImageVector(),
            contentDescription = "닫기",
            tint = BuyOrNotTheme.colors.gray500,
            modifier = Modifier
                .size(HomeBannerDefaults.CloseButtonSize)
                .padding(HomeBannerDefaults.CloseIconPadding)
        )
    }
}

/**
 * 홈 배너의 메인 콘텐츠 영역
 * 배너 이미지와 액션 버튼을 포함
 *
 * @param modifier 컴포넌트에 적용할 Modifier
 */
@Composable
private fun HomeBannerContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HomeBannerImage()
        HomeBannerActionButton(text = "고민되는 소비가 있나요?")
    }
}

/**
 * 홈 배너의 이미지 컴포넌트
 */
@Composable
private fun HomeBannerImage() {
    Image(
        painter = painterResource(id = BuyOrNotImgs.HomeBanner.resId),
        contentDescription = null,
        modifier = Modifier.size(
            width = HomeBannerDefaults.ImageWidth,
            height = HomeBannerDefaults.ImageHeight
        )
    )
}

/**
 * 홈 배너의 액션 버튼 컴포넌트
 *
 * @param text 버튼에 표시할 텍스트
 * @param modifier 컴포넌트에 적용할 Modifier
 */
@Composable
private fun HomeBannerActionButton(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(HomeBannerDefaults.ActionButtonHeight)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        HomeBannerDefaults.GradientStartColor,
                        HomeBannerDefaults.GradientEndColor
                    ),
                    radius = HomeBannerDefaults.ActionButtonGradientRadius
                ),
                shape = RoundedCornerShape(HomeBannerDefaults.ActionButtonCornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = BuyOrNotTheme.typography.subTitleS4SemiBold,
            color = BuyOrNotTheme.colors.gray0
        )
    }
}

/**
 * 홈 배너 하단의 구분선
 *
 * @param modifier 컴포넌트에 적용할 Modifier
 */
@Composable
private fun HomeBannerDivider(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(HomeBannerDefaults.DividerHeight)
            .background(BuyOrNotTheme.colors.gray300)
    )
}

@Preview(name = "HomeBanner - Pixel 5", device = "id:pixel_5", showBackground = true)
@Composable
private fun HomeBannerPreview() {
    BuyOrNotTheme {
        HomeBanner(
            onDismiss = {},
            onClick = {}
        )
    }
}
