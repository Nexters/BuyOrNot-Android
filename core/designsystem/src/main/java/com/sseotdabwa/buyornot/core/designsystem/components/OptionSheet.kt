package com.sseotdabwa.buyornot.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

/**
 * [OptionSheet]는 여러 선택지 중 하나를 선택할 수 있는 시트입니다.
 * * @param title 시트 상단에 표시될 제목입니다.
 * @param options 선택 가능한 옵션 리스트입니다.
 * @param selectedOption 현재 선택된 옵션 (체크 표시 아이콘 노출 여부 결정)입니다.
 * @param onOptionClick 옵션이 클릭되었을 때 호출되며, 선택된 옵션을 전달합니다.
 * @param onDismissRequest 시트가 닫힐 때 호출됩니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionSheet(
    title: String,
    options: List<String>,
    selectedOption: String? = null,
    sheetShape: Shape = RoundedCornerShape(26.dp),
    onOptionClick: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    BuyOrNotBottomSheet(
        onDismissRequest = onDismissRequest,
        isHalfExpandedOnly = true,
        sheetShape = sheetShape,
    ) { hideSheet ->
        val listState = rememberLazyListState()
        val isOverflowing by remember {
            derivedStateOf { listState.canScrollForward || listState.canScrollBackward }
        }
        Box(
            modifier = Modifier.clip(sheetShape),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
            ) {
                // 제목 영역
                Text(
                    text = title,
                    style = BuyOrNotTheme.typography.subTitleS1SemiBold,
                    color = BuyOrNotTheme.colors.gray950,
                    modifier =
                        Modifier
                            .padding(horizontal = 24.dp)
                            .padding(top = 16.dp, bottom = 10.dp),
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 옵션 목록 영역
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    contentPadding = PaddingValues(bottom = if (isOverflowing) 48.dp else 0.dp),
                ) {
                    items(
                        count = options.size,
                        key = { it },
                    ) { index ->
                        val option = options[index]
                        val isSelected = option == selectedOption

                        OptionItem(
                            text = option,
                            isSelected = isSelected,
                            onClick = {
                                onOptionClick(option)
                                hideSheet()
                            },
                        )
                    }
                }
            }

            if (isOverflowing) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(70.dp)
                            .background(
                                brush =
                                    Brush.verticalGradient(
                                        colors =
                                            listOf(
                                                Color.Transparent, // 시작점 (위): 투명
                                                BuyOrNotTheme.colors.gray0, // 끝점 (아래): 배경색
                                            ),
                                    ),
                            ),
                )
            }
        }
    }
}

@Composable
private fun OptionItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clickable(onClick = onClick)
                .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = text,
            style =
                if (isSelected) {
                    BuyOrNotTheme.typography.subTitleS3SemiBold
                } else {
                    BuyOrNotTheme.typography.bodyB3Medium
                },
            color =
                if (isSelected) {
                    BuyOrNotTheme.colors.gray950
                } else {
                    BuyOrNotTheme.colors.gray700
                },
        )

        if (isSelected) {
            Icon(
                imageVector = BuyOrNotIcons.Check.asImageVector(),
                contentDescription = "Check",
                tint = BuyOrNotTheme.colors.gray950,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Option Sheet Preview")
@Composable
private fun InteractiveOptionSheetPreview() {
    BuyOrNotTheme {
        var showSheet by remember { mutableStateOf(false) }
        var selectedOption by remember { mutableStateOf<String?>(null) } // To keep track of selected option

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            PrimaryButton(
                text = "Show Option Sheet",
                onClick = { showSheet = true },
            )
        }

        if (showSheet) {
            OptionSheet(
                title = "옵션 선택",
                options =
                    List(30) {
                        "옵션 ${it + 1}"
                    },
                selectedOption = selectedOption,
                onOptionClick = { option ->
                    selectedOption = option
                    showSheet = false
                },
                onDismissRequest = {
                    showSheet = false
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF000000, name = "Option Sheet Design Preview")
@Composable
private fun OptionSheetDesignPreview() {
    BuyOrNotTheme {
        OptionSheet(
            title = "옵션 선택",
            options =
                List(10) {
                    "옵션 ${it + 1}"
                },
            selectedOption = "옵션 2",
            onOptionClick = {},
            onDismissRequest = {},
        )
    }
}
