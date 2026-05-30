package com.sseotdabwa.buyornot.core.ui.crop

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.ui.crop.geometry.HandleZone
import com.sseotdabwa.buyornot.core.ui.crop.geometry.clampTo
import com.sseotdabwa.buyornot.core.ui.crop.geometry.detectHandle
import com.sseotdabwa.buyornot.core.ui.crop.geometry.resizeFrom
import com.sseotdabwa.buyornot.core.ui.crop.geometry.translate
import com.sseotdabwa.buyornot.core.ui.crop.state.NormalizedRect

@Composable
internal fun CropOverlay(
    cropRect: NormalizedRect,
    imageBounds: Rect, // 픽셀, 이미지가 화면에서 차지하는 영역
    targetRatio: Float?, // null = 자유
    onCropRectChange: (NormalizedRect) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cropRectState = rememberUpdatedState(cropRect)
    val onCropRectChangeState = rememberUpdatedState(onCropRectChange)
    val ratioState = rememberUpdatedState(targetRatio)
    val boundsState = rememberUpdatedState(imageBounds)

    Canvas(
        modifier =
            modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    val touchRadiusPx = 48.dp.toPx()
                    val minSizePx = 96.dp.toPx()
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val bounds = boundsState.value
                        if (bounds.width <= 0f || bounds.height <= 0f) return@awaitEachGesture
                        val rectPx = cropRectState.value.toPixelRect(bounds)
                        val handle =
                            detectHandle(
                                posX = down.position.x,
                                posY = down.position.y,
                                cropRect = rectPx.toNormalizedRect(bounds),
                                touchRadius = touchRadiusPx / maxOf(bounds.width, bounds.height),
                            )
                        if (handle == HandleZone.NONE) return@awaitEachGesture
                        do {
                            val event = awaitPointerEvent()
                            val delta = event.changes.first().positionChange()
                            val currentBounds = boundsState.value
                            val current = cropRectState.value
                            val normDx = delta.x / currentBounds.width
                            val normDy = delta.y / currentBounds.height
                            val unitBounds = NormalizedRect.Full
                            val minSize = minSizePx / maxOf(currentBounds.width, currentBounds.height)
                            val newRect =
                                when (handle) {
                                    HandleZone.BODY -> current.translate(normDx, normDy).clampTo(unitBounds)
                                    else ->
                                        current
                                            .resizeFrom(handle, normDx, normDy, minSize, ratioState.value)
                                            .clampTo(unitBounds)
                                }
                            onCropRectChangeState.value(newRect)
                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                        } while (event.changes.any { it.pressed })
                    }
                },
    ) {
        val bounds = boundsState.value
        if (bounds.width <= 0f || bounds.height <= 0f) return@Canvas
        val rectPx = cropRectState.value.toPixelRect(bounds)
        drawDarkMask(rectPx)
        drawHandles(rectPx, handleLength = 20.dp, strokeWidth = 3.dp)
    }
}

private fun NormalizedRect.toPixelRect(bounds: Rect): Rect =
    Rect(
        left = bounds.left + left * bounds.width,
        top = bounds.top + top * bounds.height,
        right = bounds.left + right * bounds.width,
        bottom = bounds.top + bottom * bounds.height,
    )

private fun Rect.toNormalizedRect(bounds: Rect): NormalizedRect =
    NormalizedRect(
        left = (left - bounds.left) / bounds.width,
        top = (top - bounds.top) / bounds.height,
        right = (right - bounds.left) / bounds.width,
        bottom = (bottom - bounds.top) / bounds.height,
    )

private fun DrawScope.drawDarkMask(cropRect: Rect) {
    val maskColor = Color.Black.copy(alpha = 0.5f)
    drawRect(maskColor, topLeft = Offset.Zero, size = Size(size.width, cropRect.top))
    drawRect(maskColor, topLeft = Offset(0f, cropRect.bottom), size = Size(size.width, size.height - cropRect.bottom))
    drawRect(maskColor, topLeft = Offset(0f, cropRect.top), size = Size(cropRect.left, cropRect.height))
    drawRect(maskColor, topLeft = Offset(cropRect.right, cropRect.top), size = Size(size.width - cropRect.right, cropRect.height))
}

private fun DrawScope.drawHandles(
    cropRect: Rect,
    handleLength: Dp,
    strokeWidth: Dp,
) {
    val len = handleLength.toPx()
    val sw = strokeWidth.toPx()
    val color = Color.White
    drawLine(color, cropRect.topLeft, cropRect.topLeft + Offset(len, 0f), sw, StrokeCap.Square)
    drawLine(color, cropRect.topLeft, cropRect.topLeft + Offset(0f, len), sw, StrokeCap.Square)
    drawLine(color, cropRect.topRight, cropRect.topRight + Offset(-len, 0f), sw, StrokeCap.Square)
    drawLine(color, cropRect.topRight, cropRect.topRight + Offset(0f, len), sw, StrokeCap.Square)
    drawLine(color, cropRect.bottomLeft, cropRect.bottomLeft + Offset(len, 0f), sw, StrokeCap.Square)
    drawLine(color, cropRect.bottomLeft, cropRect.bottomLeft + Offset(0f, -len), sw, StrokeCap.Square)
    drawLine(color, cropRect.bottomRight, cropRect.bottomRight + Offset(-len, 0f), sw, StrokeCap.Square)
    drawLine(color, cropRect.bottomRight, cropRect.bottomRight + Offset(0f, -len), sw, StrokeCap.Square)
}
