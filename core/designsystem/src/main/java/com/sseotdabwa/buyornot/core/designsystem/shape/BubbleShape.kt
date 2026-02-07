package com.sseotdabwa.buyornot.core.designsystem.shape

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

class BubbleShape(
    private val cornerRadius: Dp,
    private val arrowWidth: Dp,
    private val arrowHeight: Dp,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline =
        Outline.Generic(
            Path().apply {
                val cornerRadiusPx = with(density) { cornerRadius.toPx() }
                val arrowWidthPx = with(density) { arrowWidth.toPx() }
                val arrowHeightPx = with(density) { arrowHeight.toPx() }

                // 말풍선 본체 사각형 (오른쪽 화살표 영역 제외)
                addRoundRect(
                    RoundRect(
                        rect = Rect(0f, 0f, size.width - arrowWidthPx, size.height),
                        cornerRadius = CornerRadius(cornerRadiusPx),
                    ),
                )

                // 오른쪽 중앙에 삼각형 꼬리 추가
                moveTo(size.width - arrowWidthPx, size.height / 2 - arrowHeightPx / 2)
                lineTo(size.width, size.height / 2)
                lineTo(size.width - arrowWidthPx, size.height / 2 + arrowHeightPx / 2)
                close()
            },
        )
}
