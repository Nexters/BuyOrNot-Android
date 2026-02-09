package com.sseotdabwa.buyornot.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotChip
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotTab
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotTabRow
import com.sseotdabwa.buyornot.core.designsystem.components.HomeTopBar
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme


private enum class PreviewTab {
    FEED,
    REVIEW,
}

/**
 * 필터 칩 컴포넌트
 *
 * @param text 칩에 표시할 텍스트
 * @param isSelected 선택 상태 여부
 * @param onClick 클릭 시 호출되는 콜백
 * @param modifier 컴포넌트에 적용할 Modifier
 */
@Composable
private fun Chip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) BuyOrNotTheme.colors.gray800
                else BuyOrNotTheme.colors.gray200
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = BuyOrNotTheme.typography.bodyB4Medium,
            color = if (isSelected) BuyOrNotTheme.colors.gray0
            else BuyOrNotTheme.colors.gray600
        )
    }
}

/**
 * 홈 화면
 * 투표 피드와 내 투표 탭을 제공하며, 필터 칩과 배너를 포함
 */
@Composable
fun HomeScreen() {
    // 배너 노출 상태 관리
    var isBannerVisible by rememberSaveable { mutableStateOf(true) }
    var selectedChip by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            HomeTopBar(
                onNotificationClick = {},
                onProfileClick = {}
            )
        },
        containerColor = BuyOrNotTheme.colors.gray0
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            var selectedTab by remember { mutableStateOf(PreviewTab.FEED) }

            BuyOrNotTabRow(
                selectedTabIndex = PreviewTab.entries.indexOf(selectedTab),
                modifier = Modifier.padding(start = 20.dp)
            ) {
                BuyOrNotTab(
                    title = "투표 피드",
                    selected = selectedTab == PreviewTab.FEED,
                    onClick = { selectedTab = PreviewTab.FEED }
                )
                BuyOrNotTab(
                    title = "내 투표",
                    selected = selectedTab == PreviewTab.REVIEW,
                    onClick = { selectedTab = PreviewTab.REVIEW }
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            // 칩 영역 (LazyRow로 변경하여 스크롤 가능하도록)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chipItems = listOf("전체", "진행중 투표", "마감된 투표")
                items(chipItems.size) { index ->
                    BuyOrNotChip(
                        text = chipItems[index],
                        isSelected = selectedChip == index,
                        onClick = { selectedChip = index }
                    )
                }
            }

            //Spacer(modifier = Modifier.height(15.dp))

            // 메인 피드 리스트
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 배너 영역 (로그인 로직 상태에 따라 노출)
                if (isBannerVisible) {
                    item {
                        HomeBanner(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .padding(top = 20.dp),
                            onDismiss = { isBannerVisible = false },
                            onClick = { /* 투표 등록 페이지 이동부는 비워둠 */ }
                        )
                        // 배너가 보일 때만 하단 여백 추가
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }



                // 투표 피드 아이템들 (추후 구현)
                items(10) {
                    // VoteItem() 등의 컴포넌트 위치
                }
            }
        }
    }
}

@Preview(name = "HomeScreen - Pixel 5", device = "id:pixel_5", showBackground = true)
@Composable
private fun HomeScreenPreview() {
    BuyOrNotTheme {
        HomeScreen()
    }
}

