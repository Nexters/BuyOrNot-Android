package com.sseotdabwa.buyornot.feature.upload.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.components.BackTopBar
import com.sseotdabwa.buyornot.core.designsystem.components.CapsuleButton
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import java.text.DecimalFormat

@Composable
fun UploadScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
) {
    var priceRaw by remember { mutableStateOf("") }
    var priceFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var content by remember { mutableStateOf("") }
    val decimalFormat = remember { DecimalFormat("#,###") }
    val scrollState = rememberScrollState()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(BuyOrNotTheme.colors.gray0)
                .imePadding(),
    ) {
        BackTopBar {
            onNavigateBack()
        }

        Column(
            modifier =
                Modifier
                    .verticalScroll(scrollState)
                    .padding(
                        horizontal = 20.dp,
                    ).weight(1f),
        ) {
            Row(
                modifier = Modifier.padding(vertical = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "투표 등록",
                    style = BuyOrNotTheme.typography.subTitleS3SemiBold,
                    color = BuyOrNotTheme.colors.gray800,
                )
                Icon(
                    imageVector = BuyOrNotIcons.ArrowRight.asImageVector(),
                    contentDescription = "Arrow Right",
                    modifier = Modifier.size(14.dp),
                    tint = BuyOrNotTheme.colors.gray600,
                )
                Text(
                    text = "카테고리 추가",
                    style = BuyOrNotTheme.typography.subTitleS3SemiBold,
                    color = BuyOrNotTheme.colors.gray600,
                )
            }

            HorizontalDivider(
                thickness = 2.dp,
                color = BuyOrNotTheme.colors.gray100,
            )

            // 4. 가격 입력 필드
            Row(
                modifier = Modifier.padding(vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = BuyOrNotIcons.Won.asImageVector(),
                    contentDescription = "Won",
                    modifier = Modifier.size(18.dp),
                    tint = BuyOrNotTheme.colors.gray600,
                )
                Spacer(modifier = Modifier.width(6.dp))

                BasicTextField(
                    value = priceFieldValue,
                    onValueChange = { newValue ->
                        val newDigits = newValue.text.filter { it.isDigit() }

                        if (newDigits.length <= 10) {
                            priceRaw = newDigits
                            val formattedText =
                                if (newDigits.isEmpty()) {
                                    ""
                                } else {
                                    decimalFormat.format(newDigits.toLongOrNull() ?: 0)
                                }

                            // 커서 앞에 있는 숫자 개수를 세어서 새 포맷된 텍스트에서 같은 위치 찾기
                            val cursorPos = newValue.selection.end
                            val digitsBeforeCursor = newValue.text.take(cursorPos).count { it.isDigit() }

                            // 새 포맷된 텍스트에서 같은 수의 숫자를 지나간 위치 찾기
                            var digitCount = 0
                            var newCursorPos = 0
                            for (i in formattedText.indices) {
                                if (formattedText[i].isDigit()) {
                                    digitCount++
                                }
                                if (digitCount == digitsBeforeCursor) {
                                    newCursorPos = i + 1
                                    break
                                }
                            }
                            if (digitsBeforeCursor == 0) {
                                newCursorPos = 0
                            }

                            priceFieldValue =
                                TextFieldValue(
                                    text = formattedText,
                                    selection = TextRange(newCursorPos),
                                )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle =
                        BuyOrNotTheme.typography.subTitleS3SemiBold.copy(
                            color = BuyOrNotTheme.colors.gray800,
                        ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    decorationBox = { innerTextField ->
                        if (priceRaw.isEmpty()) {
                            Text(
                                text = "상품 가격을 입력해주세요",
                                style = BuyOrNotTheme.typography.subTitleS3SemiBold,
                                color = BuyOrNotTheme.colors.gray600,
                            )
                        }
                        innerTextField()
                    },
                )
            }

            HorizontalDivider(
                thickness = 2.dp,
                color = BuyOrNotTheme.colors.gray100,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                BasicTextField(
                    value = content,
                    onValueChange = { if (it.length <= 100) content = it },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 84.dp),
                    textStyle =
                        BuyOrNotTheme.typography.paragraphP2Medium.copy(
                            color = BuyOrNotTheme.colors.gray900,
                        ),
                    decorationBox = { innerTextField ->
                        if (content.isEmpty()) {
                            Text(
                                text = "고민 이유를 자세히 적을수록 더 정확한 투표 결과를 얻을 수 있어요!",
                                style = BuyOrNotTheme.typography.paragraphP2Medium,
                                color = BuyOrNotTheme.colors.gray600,
                            )
                        }
                        innerTextField()
                    },
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 글자 수 표시 (오른쪽 하단)
                Text(
                    text = "${content.length}/100",
                    modifier = Modifier.align(Alignment.End),
                    style = BuyOrNotTheme.typography.captionC3Medium,
                    color = BuyOrNotTheme.colors.gray600,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. 이미지 선택 버튼
            Surface(
                modifier = Modifier.size(68.dp),
                shape = RoundedCornerShape(12.dp),
                color = BuyOrNotTheme.colors.gray100,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement =
                        Arrangement.spacedBy(
                            space = 2.dp,
                            alignment = Alignment.CenterVertically,
                        ),
                ) {
                    Icon(
                        imageVector = BuyOrNotIcons.Camera.asImageVector(),
                        contentDescription = "Camera",
                        modifier = Modifier.size(20.dp),
                        tint = BuyOrNotTheme.colors.gray600,
                    )
                    Text(
                        text = "0/1",
                        style = BuyOrNotTheme.typography.subTitleS5SemiBold,
                        color = BuyOrNotTheme.colors.gray600,
                    )
                }
            }
        }

        // 바텀바 형태의 투표 게시 버튼
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            CapsuleButton(
                text = "투표 게시!",
                enabled = false,
            ) {
            }
        }
    }
}

@Preview
@Composable
private fun UploadScreenPreview() {
    BuyOrNotTheme {
        UploadScreen()
    }
}
