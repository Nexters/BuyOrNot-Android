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

enum class BuyOrNotTab(
    val title: String,
) {
    FEED("투표 피드"),
    REVIEW("상품 리뷰"),
}

private data class TabPosition(
    val left: Dp,
    val width: Dp,
)

@Composable
fun BuyOrNotTabRow(
    selectedTab: BuyOrNotTab,
    onTabSelected: (BuyOrNotTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        // 먼저 모든 탭을 측정
        val tabMeasurables =
            subcompose("tabs") {
                BuyOrNotTab.entries.forEach { tab ->
                    Box(
                        modifier =
                            Modifier
                                .clickable { onTabSelected(tab) }
                                .padding(horizontal = 4.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = tab.title,
                            style =
                                if (selectedTab == tab) {
                                    BuyOrNotTheme.typography.titleT4Bold
                                } else {
                                    BuyOrNotTheme.typography.bodyB4Medium
                                },
                            color = BuyOrNotTheme.colors.gray1000,
                        )
                    }
                }
            }

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
        val selectedIndex = BuyOrNotTab.entries.indexOf(selectedTab)
        val indicatorPlaceables =
            if (selectedIndex >= 0 && selectedIndex < tabPositions.size) {
                val position = tabPositions[selectedIndex]
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
            if (selectedIndex >= 0 && selectedIndex < tabPositions.size) {
                val position = tabPositions[selectedIndex]
                indicatorPlaceables.forEach { placeable ->
                    placeable.placeRelative(position.left.roundToPx(), tabHeight)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BuyOrNotTabRowPreview() {
    var selectedTab by remember { mutableStateOf(BuyOrNotTab.FEED) }

    BuyOrNotTheme {
        Column {
            BuyOrNotTabRow(
                selectedTab = selectedTab,
                onTabSelected = {
                    selectedTab = it
                },
            )
        }
    }
}
