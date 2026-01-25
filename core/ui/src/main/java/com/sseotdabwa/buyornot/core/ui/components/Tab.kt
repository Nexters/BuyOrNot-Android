package com.sseotdabwa.buyornot.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

private data class TabPosition(
    val left: Dp,
    val width: Dp,
)

/**
 * 개별 탭 컴포넌트
 *
 * @param title 탭 타이틀
 * @param selected 선택 여부
 * @param onClick 클릭 시 콜백
 * @param modifier Modifier
 */
@Composable
fun Tab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .clickable { onClick() }
                .padding(horizontal = 4.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style =
                if (selected) {
                    BuyOrNotTheme.typography.titleT4Bold
                } else {
                    BuyOrNotTheme.typography.bodyB4Medium
                },
            color = BuyOrNotTheme.colors.gray1000,
        )
    }
}

/**
 * TabRow 컴포넌트 - 슬롯 형식
 *
 * @param selectedTabIndex 현재 선택된 탭 인덱스
 * @param modifier Modifier
 * @param tabs 탭 슬롯 (Tab Composable들을 배치)
 */
@Composable
fun TabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    tabs: @Composable () -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        // 먼저 모든 탭을 측정
        val tabMeasurables = subcompose("tabs", tabs)

        // 각 탭의 너비 측정
        val tabPlaceables = tabMeasurables.map { it.measure(constraints) }
        val tabHeight = tabPlaceables.maxOfOrNull { it.height } ?: 0

        // 탭 위치 계산
        val tabPositions = mutableListOf<TabPosition>()
        var currentLeft = 0.dp

        tabPlaceables.forEach { placeable ->
            tabPositions.add(
                TabPosition(
                    left = currentLeft,
                    width = placeable.width.toDp(),
                ),
            )
            currentLeft += placeable.width.toDp() + 14.dp // spacing
        }

        val totalWidth =
            if (tabPositions.isNotEmpty()) {
                tabPositions.last().left + tabPositions.last().width
            } else {
                0.dp
            }

        // indicator 측정 (선택된 탭에만 표시)
        val indicatorPlaceables =
            if (selectedTabIndex >= 0 && selectedTabIndex < tabPositions.size) {
                val position = tabPositions[selectedTabIndex]
                val indicatorMeasurables =
                    subcompose("indicator") {
                        Box(
                            modifier =
                                Modifier
                                    .width(position.width)
                                    .height(3.dp)
                                    .background(BuyOrNotTheme.colors.gray1000),
                        )
                    }
                indicatorMeasurables.map {
                    it.measure(
                        Constraints.fixed(
                            position.width.roundToPx(),
                            3.dp.roundToPx(),
                        ),
                    )
                }
            } else {
                emptyList()
            }

        // 레이아웃
        layout(totalWidth.roundToPx(), tabHeight + 3.dp.roundToPx()) {
            // 탭 배치
            var left = 0
            tabPlaceables.forEach { placeable ->
                placeable.placeRelative(left, 0)
                left += placeable.width + 14.dp.roundToPx()
            }

            // indicator 배치
            if (selectedTabIndex >= 0 && selectedTabIndex < tabPositions.size) {
                val position = tabPositions[selectedTabIndex]
                indicatorPlaceables.forEach { placeable ->
                    placeable.placeRelative(position.left.roundToPx(), tabHeight)
                }
            }
        }
    }
}

// Preview용 enum
private enum class PreviewTab {
    FEED,
    REVIEW,
}

@Preview(showBackground = true)
@Composable
private fun TabRowPreview() {
    var selectedTab by remember { mutableStateOf(PreviewTab.FEED) }

    BuyOrNotTheme {
        Column {
            TabRow(
                selectedTabIndex = PreviewTab.entries.indexOf(selectedTab),
            ) {
                Tab(
                    title = "투표 피드",
                    selected = selectedTab == PreviewTab.FEED,
                    onClick = { selectedTab = PreviewTab.FEED },
                )
                Tab(
                    title = "상품 리뷰",
                    selected = selectedTab == PreviewTab.REVIEW,
                    onClick = { selectedTab = PreviewTab.REVIEW },
                )
            }
        }
    }
}
