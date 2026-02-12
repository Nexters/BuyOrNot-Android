package com.sseotdabwa.buyornot.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotImgs
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

/**
 * 에러 발생 시 표시되는 공통 에러 뷰 컴포넌트
 *
 * @param modifier Modifier
 * @param message 표시할 에러 메시지
 * @param onRefreshClick 새로고침 버튼 클릭 콜백
 */
@Composable
fun BuyOrNotErrorView(
    modifier: Modifier = Modifier,
    message: String = "내용을 불러오지 못했어요",
    onRefreshClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //에러 이미지
        Image(
            painter = painterResource(id = BuyOrNotImgs.Error.resId),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = BuyOrNotTheme.typography.titleT1Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        // 새로고침 버튼
        NeutralButton(
            text = "새로고침",
            onClick = onRefreshClick,
            size = ButtonSize.Small
        )
    }
}

@Preview(name = "SubPage Error", showBackground = true)
@Composable
private fun SubPageErrorPreview() {
    BuyOrNotTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            BackTopBarWithTitle(title = "투표 상세", onBackClick = {})
            BuyOrNotDivider(size = BuyOrNotDividerSize.Small)
            // 전체 화면 중앙에 에러 표시
            BuyOrNotErrorView(onRefreshClick = {})
        }
    }
}

@Preview(name = "Guest Error", showBackground = true)
@Composable
private fun GuestErrorPreview() {
    BuyOrNotTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            GuestTopBar(onLoginClick = {})
            BuyOrNotDivider(size = BuyOrNotDividerSize.Small)
            BuyOrNotErrorView(
                message = "로그인 후 이용할 수 있는 서비스입니다",
                onRefreshClick = {}
            )
        }
    }
}

@Preview(name = "Back Only Error", showBackground = true)
@Composable
private fun BackOnlyErrorPreview() {
    BuyOrNotTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            BackTopBar(onBackClick = {})
            // 메시지를 상황에 맞게 변경 가능
            BuyOrNotErrorView(
                message = "일시적인 네트워크 오류가 발생했습니다",
                onRefreshClick = {}
            )
        }
    }
}

@Preview(name = "Double Error", showBackground = true)
@Composable
private fun DoubleErrorPreview() {
    BuyOrNotTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            GuestTopBar(onLoginClick = {})
            BuyOrNotDivider(size = BuyOrNotDividerSize.Small)
            BuyOrNotTabRow(
                selectedTabIndex = 0,
                modifier = Modifier.width(300.dp).padding(start = 20.dp)
            ) {
                BuyOrNotTab(
                    title = "투표 피드",
                    selected = true,
                    onClick = {}
                )
                BuyOrNotTab(
                    title = "내 투표",
                    selected = true,
                    onClick = {}
                )
            }
            // 메시지를 상황에 맞게 변경 가능
            BuyOrNotErrorView(
                message = "일시적인 네트워크 오류가 발생했습니다",
                onRefreshClick = {}
            )
        }
    }
}

