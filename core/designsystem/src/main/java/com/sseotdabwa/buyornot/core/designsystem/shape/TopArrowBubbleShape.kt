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

class TopArrowBubbleShape(
    private val cornerRadius: Dp,
    private val arrowWidth: Dp,
    private val arrowHeight: Dp,
    private val arrowOffsetFromRight: Dp,
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
                val arrowOffsetPx = with(density) { arrowOffsetFromRight.toPx() }

                // 말풍선 본체 (상단 화살표 영역 제외)
                addRoundRect(
                    RoundRect(
                        rect = Rect(0f, arrowHeightPx, size.width, size.height),
                        cornerRadius = CornerRadius(cornerRadiusPx),
                    ),
                )

                // 상단 우측에 위를 향하는 삼각형 꼬리
                val arrowCenterX =
                    (size.width - arrowOffsetPx).coerceIn(
                        minimumValue = cornerRadiusPx + arrowWidthPx / 2,
                        maximumValue = size.width - cornerRadiusPx - arrowWidthPx / 2,
                    )
                moveTo(arrowCenterX - arrowWidthPx / 2, arrowHeightPx)
                lineTo(arrowCenterX, 0f)
                lineTo(arrowCenterX + arrowWidthPx / 2, arrowHeightPx)
                close()
            },
        )
}
