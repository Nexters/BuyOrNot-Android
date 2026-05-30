package com.sseotdabwa.buyornot.core.ui.crop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.IconResource
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

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
                .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActionButton(label = "자르기", icon = BuyOrNotIcons.Crop, onClick = onCropClick)
        Spacer(modifier = Modifier.width(90.dp))
        ActionButton(label = "회전", icon = BuyOrNotIcons.Rotate, onClick = onRotateClick)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun IdleActionBarPreview() {
    IdleActionBar(
        onCropClick = {},
        onRotateClick = {},
    )
}

@Composable
private fun ActionButton(
    label: String,
    icon: IconResource,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .height(30.dp)
                .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon.asImageVector(),
            contentDescription = label,
            tint = BuyOrNotTheme.colors.gray0,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = BuyOrNotTheme.typography.bodyB4Medium,
            color = BuyOrNotTheme.colors.gray0,
        )
    }
}
