package com.sseotdabwa.buyornot.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import kotlinx.coroutines.launch

/**
 * [BuyOrNotBottomSheet]은 드래그 및 해제 가능한 UI 요소를 제공하는 맞춤 설정 가능한 모달 바텀 시트 컴포저블입니다.
 * Material3의 [ModalBottomSheet]를 기반으로 구축되었으며, 상태 및 동작에 대한 향상된 제어 기능을 제공합니다.
 *
 * 이 바텀 시트는 자체 가시성 애니메이션(표시 및 숨기기)을 처리합니다.
 * 시트는 사용자 정의 드래그 핸들을 제공하며, 컨테이너 및 스크림에 특정 스타일을 적용합니다.
 *
 * @param onDismissRequest 시트가 닫힐 때 호출될 람다입니다. 일반적으로 시트의 컴포지션을 제어하는
 *   상태 변수를 업데이트하는 데 사용됩니다. 시트는 먼저 숨기기 애니메이션을 수행한 다음 이 람다가 호출됩니다.
 * @param isHalfExpandedOnly 바텀 시트가 화면의 절반 높이까지만 확장되도록 제한할지 여부를 결정합니다. `true`로 설정하면 시트의 최대 높이가 화면 높이의 절반으로 고정됩니다.
 * @param sheetState 바텀 시트의 상태를 제어하는 [SheetState] 객체입니다.
 *   시트를 프로그래밍 방식으로 표시하거나 숨기거나 애니메이션화하는 데 사용할 수 있습니다.
 *   기본값은 `skipPartiallyExpanded = true`가 설정된 [rememberModalBottomSheetState]로,
 *   시트가 완전히 표시되거나 숨겨지도록 합니다.
 * @param content 바텀 시트 내부에 표시될 컴포저블 콘텐츠입니다.
 *   콘텐츠 내에서 프로그래밍 방식으로 시트를 애니메이션과 함께 숨기기 위해 호출할 수 있는
 *   `hideSheet: () -> Unit`를 리시버로 제공합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyOrNotBottomSheet(
    onDismissRequest: () -> Unit,
    isHalfExpandedOnly: Boolean = false,
    sheetShape: Shape = RoundedCornerShape(26.dp),
    sheetState: SheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        ),
    content: @Composable ColumnScope.(hideSheet: () -> Unit) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val windowInfo = LocalWindowInfo.current
    val screenHeight = with(LocalDensity.current) { windowInfo.containerSize.height.toDp() }

    val hideSheetWithAnimation: () -> Unit = {
        if (sheetState.targetValue != SheetValue.Hidden) {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    onDismissRequest()
                }
            }
        }
    }

    val dimColor = BuyOrNotTheme.colors.gray1000
    val dimVisible = sheetState.targetValue != SheetValue.Hidden || sheetState.currentValue != SheetValue.Hidden
    val dimAlpha by animateFloatAsState(
        targetValue = if (dimVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "scrimAlpha",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = dimColor, alpha = (dimAlpha * 0.5f).coerceIn(0f, 1f))
        }

        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            modifier =
                Modifier
                    .padding(horizontal = 14.dp),
            sheetState = sheetState,
            shape = sheetShape,
            containerColor = Color.Transparent, // 배경 투명하게 -> Spacer
            tonalElevation = 0.dp,
            scrimColor = Color.Transparent,
            dragHandle = null,
        ) {
            // 실제 보이는 시트 컨테이너
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                color = BuyOrNotTheme.colors.gray0,
                                shape = sheetShape,
                            ).then(
                                if (isHalfExpandedOnly) {
                                    Modifier.heightIn(max = screenHeight / 2f)
                                } else {
                                    Modifier
                                },
                            ),
                ) {
                    // 드래그 핸들
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                    ) {
                        Spacer(
                            modifier =
                                Modifier
                                    .align(Alignment.Center)
                                    .width(40.dp)
                                    .height(4.dp)
                                    .background(
                                        color = Color(0xFFD9D9D9),
                                        shape = RoundedCornerShape(18.dp),
                                    ),
                        )
                    }

                    // 콘텐츠
                    content(hideSheetWithAnimation)
                }

                Spacer(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding() // 시스템 네비게이션 바 대응
                            .height(20.dp), // 하단에서 띄우고 싶은 만큼 높이 설정
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Interactive Custom Floating Sheet")
@Composable
private fun InteractiveBuyOrNotSheetPreview() {
    BuyOrNotTheme {
        var showSheet by remember { mutableStateOf(false) }

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            PrimaryButton(
                text = "Show Bottom Sheet",
                onClick = { showSheet = true },
            )
        }

        if (showSheet) {
            BuyOrNotBottomSheet(
                onDismissRequest = { showSheet = false },
                content = { hideSheet ->
                    Column(
                        modifier = Modifier.padding(18.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(50.dp)
                                    .background(
                                        color = BuyOrNotTheme.colors.gray300,
                                        shape = RoundedCornerShape(14.dp),
                                    ),
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "제목",
                            style = BuyOrNotTheme.typography.subTitleS1SemiBold,
                            color = BuyOrNotTheme.colors.gray950,
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "설명",
                            style = BuyOrNotTheme.typography.bodyB4Medium,
                            color = BuyOrNotTheme.colors.gray700,
                        )

                        Spacer(modifier = Modifier.height(26.dp))

                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(BuyOrNotTheme.colors.gray300),
                        )

                        Spacer(modifier = Modifier.height(26.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            NeutralButton(
                                text = "아니요",
                                modifier = Modifier.weight(1f),
                            ) {
                                hideSheet()
                            }

                            PrimaryButton(
                                text = "예",
                                modifier = Modifier.weight(1f),
                            ) {
                                hideSheet()
                            }
                        }
                    }
                },
            )
        }
    }
}
