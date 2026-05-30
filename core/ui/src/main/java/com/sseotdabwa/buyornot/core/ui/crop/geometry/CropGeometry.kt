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

/**
 * `quarters` 회만큼 NormalizedRect를 반시계 90° 회전 좌표계로 매핑한다.
 *
 * 회전 단위: 한 번 = CCW 90°. `quarters`가 음수이거나 4 이상이면 mod 4로 정규화한다.
 *
 * 회전된 비트맵 좌표계 기준이므로, `EditSpec.rotationQuarters`가 변경될 때
 * `EditSpec.crop.rectNormalized`를 새 좌표계로 옮기는 용도로 사용한다.
 */
fun NormalizedRect.rotateCounterClockwise(quarters: Int): NormalizedRect {
    val n = ((quarters % 4) + 4) % 4
    var r = this
    repeat(n) {
        r =
            NormalizedRect(
                left = r.top,
                top = 1f - r.right,
                right = r.bottom,
                bottom = 1f - r.left,
            )
    }
    return r
}

/**
 * 핸들 드래그로 rect를 리사이즈한다.
 *
 * Precondition (targetRatio != null): `this` rect already satisfies `targetRatio`
 * (width / height ≈ targetRatio). 그렇지 않은 상태에서 호출하면 첫 제스처에서
 * height가 width 기준으로 silently 재계산되어 의도치 않은 형태가 될 수 있다.
 * 비율을 새로 적용한 직후라면 먼저 [computeRectForRatio]를 호출해 rect를 정규화하라.
 */
fun NormalizedRect.resizeFrom(
    corner: HandleZone,
    deltaX: Float,
    deltaY: Float,
    minSize: Float,
    targetRatio: Float?,
): NormalizedRect {
    when (corner) {
        HandleZone.TL, HandleZone.TR, HandleZone.BL, HandleZone.BR -> Unit
        else -> return this
    }

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
    val l = (cx - maxW / 2f).coerceIn(bounds.left, bounds.right - maxW)
    val t = (cy - maxH / 2f).coerceIn(bounds.top, bounds.bottom - maxH)
    return NormalizedRect(l, t, l + maxW, t + maxH)
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
