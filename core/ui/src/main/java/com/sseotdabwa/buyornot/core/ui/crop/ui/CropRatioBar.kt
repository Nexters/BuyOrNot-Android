package com.sseotdabwa.buyornot.core.ui.crop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
                .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RatioItem("자유형태", AspectRatio.Free, selected, onSelect)
        RatioItem("1:1", AspectRatio.R1x1, selected, onSelect)
        RatioItem("3:4", AspectRatio.R3x4, selected, onSelect)
        RatioItem("4:3", AspectRatio.R4x3, selected, onSelect)
    }
}

@Composable
private fun RatioItem(
    label: String,
    ratio: AspectRatio,
    selected: AspectRatio,
    onSelect: (AspectRatio) -> Unit,
) {
    val isSelected = selected == ratio
    Box(
        modifier =
            Modifier
                .clickable { onSelect(ratio) }
                .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
        )
    }
}
