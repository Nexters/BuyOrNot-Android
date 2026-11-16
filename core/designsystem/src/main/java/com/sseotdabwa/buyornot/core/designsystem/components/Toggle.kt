package com.sseotdabwa.buyornot.core.designsystem.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

private val ToggleWidth = 42.dp
private val ToggleHeight = 26.dp
private val TogglePadding = 2.dp
private val ThumbSize = ToggleHeight - TogglePadding * 2

@Composable
fun BuyOrNotToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val trackColor =
        when {
            enabled && checked -> BuyOrNotTheme.colors.gray950
            enabled -> BuyOrNotTheme.colors.gray400
            checked -> BuyOrNotTheme.colors.gray600
            else -> BuyOrNotTheme.colors.gray200
        }
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) ToggleWidth - ThumbSize - TogglePadding * 2 else 0.dp,
        label = "toggleThumbOffset",
    )

    Row(
        modifier =
            modifier
                .size(width = ToggleWidth, height = ToggleHeight)
                .clip(CircleShape)
                .background(color = trackColor)
                .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
                .padding(TogglePadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .offset(x = thumbOffset)
                    .size(ThumbSize)
                    .shadow(elevation = 2.dp, shape = CircleShape)
                    .background(color = BuyOrNotTheme.colors.gray0, shape = CircleShape),
        )
    }
}

@Preview
@Composable
private fun BuyOrNotTogglePreview() {
    BuyOrNotTheme {
        Row {
            BuyOrNotToggle(checked = true, onCheckedChange = {})
            BuyOrNotToggle(checked = false, onCheckedChange = {})
            BuyOrNotToggle(checked = true, onCheckedChange = {}, enabled = false)
            BuyOrNotToggle(checked = false, onCheckedChange = {}, enabled = false)
        }
    }
}
