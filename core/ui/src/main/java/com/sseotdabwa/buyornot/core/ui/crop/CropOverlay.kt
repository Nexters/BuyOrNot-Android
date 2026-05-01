package com.sseotdabwa.buyornot.core.ui.crop

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
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
