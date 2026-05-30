package com.sseotdabwa.buyornot.core.ui.crop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.IconResource
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.core.ui.crop.state.AspectRatio

@Composable
internal fun CropRatioBar(
    selected: AspectRatio,
    onSelect: (AspectRatio) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(
                    top = 36.dp,
                    bottom = 56.dp,
                ),
        horizontalArrangement =
            Arrangement.spacedBy(
                space = 16.dp,
                alignment = Alignment.CenterHorizontally,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RatioItem("자유형태", BuyOrNotIcons.RatioFree, 0.dp, 7.dp, AspectRatio.Free, selected, onSelect)
        RatioItem("1:1", BuyOrNotIcons.Ratio1x1, 5.dp, 8.dp, AspectRatio.R1x1, selected, onSelect)
        RatioItem("3:4", BuyOrNotIcons.Ratio3x4, 0.dp, 5.dp, AspectRatio.R3x4, selected, onSelect)
        RatioItem("4:3", BuyOrNotIcons.Ratio4x3, 5.dp, 8.dp, AspectRatio.R4x3, selected, onSelect)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun CropRatioBarFreePreview() {
    CropRatioBar(selected = AspectRatio.Free, onSelect = {})
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun CropRatioBar1x1Preview() {
    CropRatioBar(selected = AspectRatio.R1x1, onSelect = {})
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun CropRatioBar3x4Preview() {
    CropRatioBar(selected = AspectRatio.R3x4, onSelect = {})
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun CropRatioBar4x3Preview() {
    CropRatioBar(selected = AspectRatio.R4x3, onSelect = {})
}

@Composable
private fun RatioItem(
    label: String,
    icon: IconResource,
    iconTopPadding: Dp,
    iconToTextGap: Dp,
    ratio: AspectRatio,
    selected: AspectRatio,
    onSelect: (AspectRatio) -> Unit,
) {
    val isSelected = selected == ratio
    val tint = if (isSelected) BuyOrNotTheme.colors.gray0 else BuyOrNotTheme.colors.gray700
    Column(
        modifier =
            Modifier
                .height(60.dp)
                .clickable { onSelect(ratio) }
                .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(iconTopPadding))
        Icon(
            imageVector = icon.asImageVector(),
            contentDescription = label,
            tint = tint,
        )
        Spacer(modifier = Modifier.height(iconToTextGap))
        Text(
            text = label,
            style = BuyOrNotTheme.typography.bodyB5Medium,
            color = tint,
        )
    }
}
