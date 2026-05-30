package com.sseotdabwa.buyornot.core.ui.crop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector

@Composable
internal fun IdleActionBar(
    onCropClick: () -> Unit,
    onRotateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(vertical = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActionButton(label = "자르기", onClick = onCropClick)
        androidx.compose.foundation.layout
            .Spacer(modifier = Modifier.width(90.dp))
        ActionButton(label = "회전", onClick = onRotateClick)
    }
}

@Composable
private fun ActionButton(
    label: String,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = BuyOrNotIcons.Close.asImageVector(),
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(20.dp),
        )
        Text(text = label, color = Color.White)
    }
}
