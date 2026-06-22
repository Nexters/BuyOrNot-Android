package com.sseotdabwa.buyornot.core.ui.crop.processing

import com.sseotdabwa.buyornot.core.ui.crop.state.NormalizedRect

fun mapNormalizedToPixel(
    rect: NormalizedRect,
    bitmapWidth: Int,
    bitmapHeight: Int,
): PixelRect {
    val srcX = (rect.left * bitmapWidth).toInt().coerceIn(0, bitmapWidth - 1)
    val srcY = (rect.top * bitmapHeight).toInt().coerceIn(0, bitmapHeight - 1)
    val rawW = (rect.width * bitmapWidth).toInt()
    val rawH = (rect.height * bitmapHeight).toInt()
    val srcW = rawW.coerceIn(1, bitmapWidth - srcX)
    val srcH = rawH.coerceIn(1, bitmapHeight - srcY)
    return PixelRect(srcX, srcY, srcW, srcH)
}
