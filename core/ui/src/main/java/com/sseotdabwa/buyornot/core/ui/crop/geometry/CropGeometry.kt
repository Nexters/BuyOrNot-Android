package com.sseotdabwa.buyornot.core.ui.crop.geometry

import com.sseotdabwa.buyornot.core.ui.crop.state.NormalizedRect
import kotlin.math.abs
import kotlin.math.hypot

enum class HandleZone { TL, TR, BL, BR, BODY, NONE }

fun NormalizedRect.clampTo(bounds: NormalizedRect): NormalizedRect {
    val maxW = bounds.right - bounds.left
    val maxH = bounds.bottom - bounds.top
    val w = (right - left).coerceAtMost(maxW)
    val h = (bottom - top).coerceAtMost(maxH)
    val newLeft = left.coerceIn(bounds.left, bounds.right - w)
    val newTop = top.coerceIn(bounds.top, bounds.bottom - h)
    return NormalizedRect(newLeft, newTop, newLeft + w, newTop + h)
}

fun NormalizedRect.translate(
    dx: Float,
    dy: Float,
): NormalizedRect = NormalizedRect(left + dx, top + dy, right + dx, bottom + dy)

fun NormalizedRect.resizeFrom(
    corner: HandleZone,
    deltaX: Float,
    deltaY: Float,
    minSize: Float,
    targetRatio: Float?,
): NormalizedRect {
    if (corner !in setOf(HandleZone.TL, HandleZone.TR, HandleZone.BL, HandleZone.BR)) return this

    if (targetRatio == null) {
        var l = left
        var t = top
        var r = right
        var b = bottom
        when (corner) {
            HandleZone.TL -> {
                l += deltaX
                t += deltaY
            }
            HandleZone.TR -> {
                r += deltaX
                t += deltaY
            }
            HandleZone.BL -> {
                l += deltaX
                b += deltaY
            }
            HandleZone.BR -> {
                r += deltaX
                b += deltaY
            }
            else -> Unit
        }
        if (r - l < minSize) {
            when (corner) {
                HandleZone.TL, HandleZone.BL -> l = r - minSize
                HandleZone.TR, HandleZone.BR -> r = l + minSize
                else -> Unit
            }
        }
        if (b - t < minSize) {
            when (corner) {
                HandleZone.TL, HandleZone.TR -> t = b - minSize
                HandleZone.BL, HandleZone.BR -> b = t + minSize
                else -> Unit
            }
        }
        return NormalizedRect(l, t, r, b)
    }

    val signedDelta =
        when (corner) {
            HandleZone.TL -> if (abs(deltaX) >= abs(deltaY)) -deltaX else -deltaY
            HandleZone.TR -> if (abs(deltaX) >= abs(deltaY)) deltaX else -deltaY
            HandleZone.BL -> if (abs(deltaX) >= abs(deltaY)) -deltaX else deltaY
            HandleZone.BR -> if (abs(deltaX) >= abs(deltaY)) deltaX else deltaY
            else -> 0f
        }
    val newWidth = (width + signedDelta).coerceAtLeast(minSize)
    val newHeight = newWidth / targetRatio
    return when (corner) {
        HandleZone.TL -> NormalizedRect(right - newWidth, bottom - newHeight, right, bottom)
        HandleZone.TR -> NormalizedRect(left, bottom - newHeight, left + newWidth, bottom)
        HandleZone.BL -> NormalizedRect(right - newWidth, top, right, top + newHeight)
        HandleZone.BR -> NormalizedRect(left, top, left + newWidth, top + newHeight)
        else -> this
    }
}

fun computeRectForRatio(
    current: NormalizedRect,
    bounds: NormalizedRect,
    targetRatio: Float?,
): NormalizedRect {
    if (targetRatio == null) return current.clampTo(bounds)
    val cx = (current.left + current.right) / 2f
    val cy = (current.top + current.bottom) / 2f
    val boundsW = bounds.right - bounds.left
    val boundsH = bounds.bottom - bounds.top
    val maxW = minOf(boundsW, boundsH * targetRatio)
    val maxH = maxW / targetRatio
    var l = cx - maxW / 2f
    var r = cx + maxW / 2f
    var t = cy - maxH / 2f
    var b = cy + maxH / 2f
    if (l < bounds.left) {
        val s = bounds.left - l
        l += s
        r += s
    }
    if (r > bounds.right) {
        val s = r - bounds.right
        l -= s
        r -= s
    }
    if (t < bounds.top) {
        val s = bounds.top - t
        t += s
        b += s
    }
    if (b > bounds.bottom) {
        val s = b - bounds.bottom
        t -= s
        b -= s
    }
    return NormalizedRect(l, t, r, b)
}

fun detectHandle(
    posX: Float,
    posY: Float,
    cropRect: NormalizedRect,
    touchRadius: Float,
): HandleZone {
    val corners =
        listOf(
            HandleZone.TL to (cropRect.left to cropRect.top),
            HandleZone.TR to (cropRect.right to cropRect.top),
            HandleZone.BL to (cropRect.left to cropRect.bottom),
            HandleZone.BR to (cropRect.right to cropRect.bottom),
        )
    for ((zone, corner) in corners) {
        if (hypot(posX - corner.first, posY - corner.second) <= touchRadius) return zone
    }
    val insideX = posX in cropRect.left..cropRect.right
    val insideY = posY in cropRect.top..cropRect.bottom
    return if (insideX && insideY) HandleZone.BODY else HandleZone.NONE
}
