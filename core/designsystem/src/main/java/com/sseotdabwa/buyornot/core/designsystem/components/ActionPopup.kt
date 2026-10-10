package com.sseotdabwa.buyornot.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import kotlinx.coroutines.launch

private val SelectableItemWidth = 90.dp

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
 * @param selectedIndex 선택된 항목(정렬 등). null이면 선택 표시 없이 모든 항목을 같은 스타일로 그린다.
 */
@Composable
fun ActionPopup(
    items: List<Pair<String, () -> Unit>>,
    onDismiss: () -> Unit,
    selectedIndex: Int? = null,
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
            selectedIndex = selectedIndex,
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
 * @param selectedIndex 선택된 항목. 선택 메뉴는 항목 폭을 고정하고 선택 항목만 굵게 표시한다.
 */
@Composable
fun ActionPopupContent(
    items: List<Pair<String, () -> Unit>>,
    modifier: Modifier = Modifier,
    selectedIndex: Int? = null,
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
        // 가장 긴 항목에 폭을 맞춰 모든 항목이 같은 폭 안에서 중앙 정렬되게 한다.
        Column(
            modifier =
                Modifier
                    .width(IntrinsicSize.Max)
                    .padding(
                        horizontal = 6.dp,
                        vertical = 10.dp,
                    ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items.forEachIndexed { index, (label, onClick) ->
                val isSelectable = selectedIndex != null
                val isSelected = index == selectedIndex
                Text(
                    text = label,
                    modifier =
                        Modifier
                            .then(if (isSelectable) Modifier.width(SelectableItemWidth) else Modifier.fillMaxWidth())
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = remember(pressedColor) { PressedColorIndicationFactory(color = pressedColor) },
                                onClick = onClick,
                            ).padding(
                                horizontal = if (isSelectable) 12.dp else 20.dp,
                                vertical = 8.dp,
                            ),
                    style = if (isSelected) BuyOrNotTheme.typography.titleT3Bold else BuyOrNotTheme.typography.bodyB3Medium,
                    color = if (isSelected) BuyOrNotTheme.colors.gray950 else BuyOrNotTheme.colors.gray800,
                    textAlign = TextAlign.Center,
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
