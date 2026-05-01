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
import kotlin.math.abs

internal enum class HandleZone { TL, TR, BL, BR, BODY, NONE }

internal fun detectHandle(
    position: Offset,
    cropRect: Rect,
    touchRadius: Float,
): HandleZone {
    val corners =
        listOf(
            HandleZone.TL to cropRect.topLeft,
            HandleZone.TR to cropRect.topRight,
            HandleZone.BL to cropRect.bottomLeft,
            HandleZone.BR to cropRect.bottomRight,
        )
    for ((zone, corner) in corners) {
        if ((position - corner).getDistance() <= touchRadius) return zone
    }
    if (cropRect.contains(position)) return HandleZone.BODY
    return HandleZone.NONE
}

internal fun Rect.resizeFrom(
    corner: HandleZone,
    delta: Offset,
    minSize: Float,
): Rect {
    val d =
        when (corner) {
            HandleZone.TL -> if (abs(delta.x) >= abs(delta.y)) -delta.x else -delta.y
            HandleZone.TR -> if (abs(delta.x) >= abs(delta.y)) delta.x else -delta.y
            HandleZone.BL -> if (abs(delta.x) >= abs(delta.y)) -delta.x else delta.y
            HandleZone.BR -> if (abs(delta.x) >= abs(delta.y)) delta.x else delta.y
            else -> return this
        }
    val newSize = (width + d).coerceAtLeast(minSize)
    return when (corner) {
        HandleZone.TL -> Rect(right - newSize, bottom - newSize, right, bottom)
        HandleZone.TR -> Rect(left, bottom - newSize, left + newSize, bottom)
        HandleZone.BL -> Rect(right - newSize, top, right, top + newSize)
        HandleZone.BR -> Rect(left, top, left + newSize, top + newSize)
        else -> this
    }
}

internal fun Rect.clampTo(bounds: Rect): Rect {
    val clampedSize = width.coerceAtMost(bounds.width).coerceAtMost(bounds.height)
    val clampedLeft = left.coerceIn(bounds.left, bounds.right - clampedSize)
    val clampedTop = top.coerceIn(bounds.top, bounds.bottom - clampedSize)
    return Rect(clampedLeft, clampedTop, clampedLeft + clampedSize, clampedTop + clampedSize)
}

@Composable
internal fun CropOverlay(
    cropRect: Rect,
    imageBounds: Rect,
    onCropRectChange: (Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cropRectState = rememberUpdatedState(cropRect)
    val imageBoundsState = rememberUpdatedState(imageBounds)
    val onCropRectChangeState = rememberUpdatedState(onCropRectChange)

    Canvas(
        modifier =
            modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    val touchRadiusPx = 48.dp.toPx()
                    val minSizePx = 96.dp.toPx()
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val handle = detectHandle(down.position, cropRectState.value, touchRadiusPx)
                        if (handle == HandleZone.NONE) return@awaitEachGesture
                        do {
                            val event = awaitPointerEvent()
                            val delta = event.changes.first().positionChange()
                            val currentRect = cropRectState.value
                            val currentBounds = imageBoundsState.value
                            val newRect =
                                when (handle) {
                                    HandleZone.BODY -> currentRect.translate(delta).clampTo(currentBounds)
                                    else -> currentRect.resizeFrom(handle, delta, minSizePx).clampTo(currentBounds)
                                }
                            onCropRectChangeState.value(newRect)
                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                        } while (event.changes.any { it.pressed })
                    }
                },
    ) {
        drawDarkMask(cropRect)
        drawHandles(cropRect, handleLength = 20.dp, strokeWidth = 3.dp)
    }
}

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

    // TL
    drawLine(color, cropRect.topLeft, cropRect.topLeft + Offset(len, 0f), sw, StrokeCap.Square)
    drawLine(color, cropRect.topLeft, cropRect.topLeft + Offset(0f, len), sw, StrokeCap.Square)
    // TR
    drawLine(color, cropRect.topRight, cropRect.topRight + Offset(-len, 0f), sw, StrokeCap.Square)
    drawLine(color, cropRect.topRight, cropRect.topRight + Offset(0f, len), sw, StrokeCap.Square)
    // BL
    drawLine(color, cropRect.bottomLeft, cropRect.bottomLeft + Offset(len, 0f), sw, StrokeCap.Square)
    drawLine(color, cropRect.bottomLeft, cropRect.bottomLeft + Offset(0f, -len), sw, StrokeCap.Square)
    // BR
    drawLine(color, cropRect.bottomRight, cropRect.bottomRight + Offset(-len, 0f), sw, StrokeCap.Square)
    drawLine(color, cropRect.bottomRight, cropRect.bottomRight + Offset(0f, -len), sw, StrokeCap.Square)
}
