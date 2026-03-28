package com.sseotdabwa.buyornot.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import kotlinx.coroutines.launch

private class PressedColorIndicationNode(
    private val color: Color,
    private val interactionSource: InteractionSource,
) : Modifier.Node(),
    DrawModifierNode {
    private var isPressed = false

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> isPressed = true
                    is PressInteraction.Release, is PressInteraction.Cancel -> isPressed = false
                }
                invalidateDraw()
            }
        }
    }

    override fun ContentDrawScope.draw() {
        if (isPressed) {
            drawRect(color = color)
        }
        drawContent()
    }
}

private data class PressedColorIndicationFactory(
    val color: Color,
) : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode = PressedColorIndicationNode(color, interactionSource)
}

/**
 * [ActionPopup]은 앵커 아이콘 하단에 메뉴 항목 목록을 팝업으로 표시하는 컴포넌트입니다.
 *
 * @param items 표시할 메뉴 항목 목록입니다. 각 항목은 레이블 문자열과 클릭 콜백의 쌍으로 구성됩니다.
 * @param onDismiss 팝업 외부 영역 클릭 시 호출되는 콜백입니다.
 */
@Composable
fun ActionPopup(
    items: List<Pair<String, () -> Unit>>,
    onDismiss: () -> Unit,
) {
    val density = LocalDensity.current
    val navHeight = 20.dp
    val spacing = 4.dp
    val offset =
        remember(density) {
            with(density) {
                IntOffset(
                    x = 0,
                    y = (navHeight + spacing).roundToPx(),
                )
            }
        }

    Popup(
        onDismissRequest = onDismiss,
        alignment = Alignment.TopEnd,
        offset = offset,
        properties = PopupProperties(focusable = true),
    ) {
        ActionPopupContent(
            items = items,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
        )
    }
}

/**
 * [ActionPopupContent]는 팝업 메뉴 항목 목록을 표시하는 컴포넌트입니다.
 *
 * @param items 표시할 메뉴 항목 목록입니다. 각 항목은 레이블 문자열과 클릭 콜백의 쌍으로 구성됩니다.
 * @param modifier 컴포넌트에 적용할 Modifier입니다.
 * @param tonalElevation Surface의 tonal elevation입니다.
 * @param shadowElevation Surface의 shadow elevation입니다.
 */
@Composable
fun ActionPopupContent(
    items: List<Pair<String, () -> Unit>>,
    modifier: Modifier = Modifier,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = BuyOrNotTheme.colors.gray0,
        border =
            BorderStroke(
                color = BuyOrNotTheme.colors.gray100,
                width = 1.dp,
            ),
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
    ) {
        val pressedColor = BuyOrNotTheme.colors.gray200
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items.forEach { (label, onClick) ->
                Text(
                    text = label,
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = remember(pressedColor) { PressedColorIndicationFactory(color = pressedColor) },
                                onClick = onClick,
                            ).padding(
                                horizontal = 20.dp,
                                vertical = 8.dp,
                            ),
                    style = BuyOrNotTheme.typography.bodyB3Medium,
                    color = BuyOrNotTheme.colors.gray800,
                )
            }
        }
    }
}

@Preview(
    name = "ActionPopupContent Preview - Owner",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun ActionPopupContentOwnerPreview() {
    BuyOrNotTheme {
        ActionPopupContent(
            items = listOf("삭제하기" to {}),
        )
    }
}

@Preview(
    name = "ActionPopupContent Preview - User",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun ActionPopupContentUserPreview() {
    BuyOrNotTheme {
        ActionPopupContent(
            items = listOf("신고하기" to {}, "차단하기" to {}),
        )
    }
}
