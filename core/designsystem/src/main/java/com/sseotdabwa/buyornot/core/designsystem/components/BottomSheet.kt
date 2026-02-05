package com.sseotdabwa.buyornot.core.designsystem.components

import android.R.attr.top
import android.view.Gravity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

@Composable
fun BuyOrNotCustomBottomSheet(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false, // 가로 길이를 Modifier에서 제어하기 위해 필수
            ),
    ) {
        // 1. 다이얼로그 윈도우 자체의 배치를 하단으로 고정
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        dialogWindow?.let { window ->
            window.setGravity(Gravity.BOTTOM) // 하단 배치
            window.setDimAmount(0.5f) // 뒷배경 흐림 정도
        }

        // 2. 내부 카드 디자인
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
            // 기기 하단 끝에서 띄우기 위한 여백
            shape = RoundedCornerShape(28.dp),
            color = BuyOrNotTheme.colors.gray0,
            tonalElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                // 상단 커스텀 핸들바
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 10.dp,
                            ),
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

                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Interactive Custom Floating Sheet")
@Composable
private fun InteractiveBuyOrNotCustomSheetPreview() {
    BuyOrNotTheme {
        var showSheet by remember { mutableStateOf(false) }

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            PrimaryButton(
                text = "Show Floating Sheet",
                onClick = { showSheet = true },
            )
        }

        if (showSheet) {
            BuyOrNotCustomBottomSheet(
                onDismissRequest = { showSheet = false },
                content = {
                    Column(
                        modifier = Modifier.padding(18.dp),
                    ) {
                        Text("여기는 컨텐츠 영역입니다.")
                    }
                },
            )
        }
    }
}
