package com.sseotdabwa.buyornot.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

/**
 * 디바이더 사이즈 타입
 */
enum class BuyOrNotDividerSize(val height: Dp) {
    Small(2.dp),
    Large(10.dp)
}

/**
 * BuyOrNot 공통 디바이더 컴포넌트
 *
 * @param size 디바이더의 두께 (Small: 2dp, Large: 10dp)
 * @param modifier 컴포넌트에 적용할 Modifier
 * @param color 디바이더 색상 (기본값 gray100/200 추천)
 */
@Composable
fun BuyOrNotDivider(
    size: BuyOrNotDividerSize,
    modifier: Modifier = Modifier,
    color: Color = BuyOrNotTheme.colors.gray100
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(size.height)
            .background(color)
    )
}

@Preview(showBackground = true)
@Composable
fun DividerPreview() {
    BuyOrNotTheme {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Small Divider (2px)")
            BuyOrNotDivider(size = BuyOrNotDividerSize.Small)

            Spacer(modifier = Modifier.height(20.dp))

            Text("Large Divider (10px)")
            BuyOrNotDivider(size = BuyOrNotDividerSize.Large)
        }
    }
}
