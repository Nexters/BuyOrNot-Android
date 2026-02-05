import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

@Composable
fun BuyOrNotChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 상태 변화를 감지하기 위한 interactionSource
    val interactionSource = remember { MutableInteractionSource() }

    // Hover 및 Pressed 상태 수집
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    // 상태별 색상 결정 로직
    val backgroundColor by animateColorAsState(
        targetValue =
            when {
                isSelected -> BuyOrNotTheme.colors.gray900 // Selected
                isPressed || isHovered -> BuyOrNotTheme.colors.gray400 // Hover/Pressed
                else -> BuyOrNotTheme.colors.gray200 // Unselected
            },
        label = "backgroundColor",
    )

    val contentColor by animateColorAsState(
        targetValue =
            when {
                isSelected -> BuyOrNotTheme.colors.gray0
                else -> BuyOrNotTheme.colors.gray700
            },
        label = "contentColor",
    )

    val textStyle =
        if (isSelected) {
            BuyOrNotTheme.typography.subTitleS5SemiBold
        } else {
            BuyOrNotTheme.typography.bodyB5Medium
        }

    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        contentColor = contentColor,
        interactionSource = interactionSource,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = textStyle,
            )
        }
    }
}

@Preview(name = "Chip Preview")
@Composable
fun BuyOrNotChipPreview() {
    BuyOrNotTheme {
        var isSelected by remember { mutableStateOf(false) }

        BuyOrNotChip(
            text = "Selected Chip",
            isSelected = isSelected,
            onClick = { isSelected = !isSelected },
        )
    }
}
