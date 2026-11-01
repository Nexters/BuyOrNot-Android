package com.sseotdabwa.buyornot.feature.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotBottomSheet
import com.sseotdabwa.buyornot.core.designsystem.components.PrimaryButton
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotImgs
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

private object FeedCreateNudgeSheetDefaults {
    val TopSpacing = 34.dp
    val HorizontalPadding = 18.dp
    val BottomPadding = 16.dp
    val TextTopPadding = 8.dp
    val TextHorizontalPadding = 6.dp
    val TextSpacing = 8.dp
    val IllustrationTopSpacing = 2.dp
    val IllustrationWidth = 276.dp
    val IllustrationHeight = 142.dp
    val ButtonTopSpacing = 26.dp
}

/**
 * 홈 투표 피드를 2행 넘게 스크롤했을 때 투표 등록을 권하는 바텀시트.
 *
 * @param onRegisterClick [투표 등록] 클릭
 * @param onDismissRequest Dim 클릭·뒤로가기로 닫힐 때
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedCreateNudgeSheet(
    onRegisterClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    BuyOrNotBottomSheet(
        onDismissRequest = onDismissRequest,
        showDragHandle = false,
    ) {
        FeedCreateNudgeContent(onRegisterClick = onRegisterClick)
    }
}

@Composable
private fun FeedCreateNudgeContent(onRegisterClick: () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    top = FeedCreateNudgeSheetDefaults.TopSpacing,
                    start = FeedCreateNudgeSheetDefaults.HorizontalPadding,
                    end = FeedCreateNudgeSheetDefaults.HorizontalPadding,
                    bottom = FeedCreateNudgeSheetDefaults.BottomPadding,
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top = FeedCreateNudgeSheetDefaults.TextTopPadding,
                        start = FeedCreateNudgeSheetDefaults.TextHorizontalPadding,
                        end = FeedCreateNudgeSheetDefaults.TextHorizontalPadding,
                    ),
            verticalArrangement = Arrangement.spacedBy(FeedCreateNudgeSheetDefaults.TextSpacing),
        ) {
            Text(
                text = "살까말까 고민되는 상품이 있으신가요?",
                style = BuyOrNotTheme.typography.titleT1Bold,
                color = BuyOrNotTheme.colors.gray950,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "고민 중인 아이템이 있다면 등록해보세요!",
                style = BuyOrNotTheme.typography.bodyB4Medium,
                color = BuyOrNotTheme.colors.gray700,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(FeedCreateNudgeSheetDefaults.IllustrationTopSpacing))

        // 그림자까지 포함한 에셋이라 시트 안쪽 폭보다 넓다. 좁은 화면에서도 줄이지 않고 가운데에서 좌우로 넘치게 둔다.
        Image(
            painter = painterResource(id = BuyOrNotImgs.FeedCreateNudge.resId),
            contentDescription = null,
            modifier =
                Modifier
                    .wrapContentWidth(unbounded = true)
                    .size(
                        width = FeedCreateNudgeSheetDefaults.IllustrationWidth,
                        height = FeedCreateNudgeSheetDefaults.IllustrationHeight,
                    ),
        )

        Spacer(modifier = Modifier.height(FeedCreateNudgeSheetDefaults.ButtonTopSpacing))

        PrimaryButton(
            text = "투표 등록",
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FeedCreateNudgeContentPreview() {
    BuyOrNotTheme {
        FeedCreateNudgeContent(onRegisterClick = {})
    }
}
