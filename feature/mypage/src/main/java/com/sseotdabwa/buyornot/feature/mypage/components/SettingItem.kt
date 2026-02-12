package com.sseotdabwa.buyornot.feature.mypage.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

@Composable
internal fun SettingItem(
    title: String,
    textColor: Color = BuyOrNotTheme.colors.gray900,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = BuyOrNotTheme.colors.gray0,
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            style = BuyOrNotTheme.typography.paragraphP1Medium,
            color = textColor,
        )
    }
}
