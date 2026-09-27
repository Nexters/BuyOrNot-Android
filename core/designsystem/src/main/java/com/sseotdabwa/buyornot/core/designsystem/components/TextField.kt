package com.sseotdabwa.buyornot.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

/**
 * 테두리형 한 줄 입력 필드
 *
 * 입력창 아래에 에러 문구(왼쪽)와 글자 수 카운터(오른쪽)를 보여준다.
 * [maxLength]는 카운터 표시에만 쓰이며, 글자 수 제한은 [onValueChange]를 넘기는 쪽에서 적용한다.
 *
 * @param label 입력창 위에 표시할 라벨. null이면 표시하지 않는다.
 * @param errorMessage null이 아니면 테두리를 에러 색으로 바꾸고 문구를 표시한다.
 * @param maxLength null이 아니면 `현재/최대` 카운터를 표시한다.
 */
@Composable
fun BuyOrNotTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String? = null,
    errorMessage: String? = null,
    maxLength: Int? = null,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val isError = errorMessage != null

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = BuyOrNotTheme.typography.subTitleS5SemiBold,
                color = BuyOrNotTheme.colors.gray800,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            textStyle =
                BuyOrNotTheme.typography.paragraphP2Medium.copy(
                    color = BuyOrNotTheme.colors.gray950,
                ),
            cursorBrush = SolidColor(BuyOrNotTheme.colors.gray950),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            decorationBox = { innerTextField ->
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                color = BuyOrNotTheme.colors.gray0,
                                shape = RoundedCornerShape(10.dp),
                            ).border(
                                width = 1.dp,
                                color = if (isError) BuyOrNotTheme.colors.red100 else BuyOrNotTheme.colors.gray300,
                                shape = RoundedCornerShape(10.dp),
                            ).padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = BuyOrNotTheme.typography.paragraphP2Medium,
                            color = BuyOrNotTheme.colors.gray500,
                        )
                    }
                    innerTextField()
                }
            },
        )

        if (isError || maxLength != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = errorMessage.orEmpty(),
                    modifier = Modifier.weight(1f),
                    style = BuyOrNotTheme.typography.captionC1Medium,
                    color = BuyOrNotTheme.colors.red100,
                )
                if (maxLength != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${value.length}/$maxLength",
                        style = BuyOrNotTheme.typography.captionC1Medium,
                        color = BuyOrNotTheme.colors.gray500,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BuyOrNotTextFieldPreview() {
    BuyOrNotTheme {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            BuyOrNotTextField(
                value = "",
                onValueChange = {},
                placeholder = "닉네임을 입력해주세요",
                maxLength = 10,
            )
            BuyOrNotTextField(
                value = "살까말까!",
                onValueChange = {},
                errorMessage = "특수문자는 사용할 수 없어요.",
                maxLength = 10,
            )
            BuyOrNotTextField(
                value = "살까말까고밍",
                onValueChange = {},
                label = "닉네임",
                maxLength = 10,
            )
        }
    }
}
