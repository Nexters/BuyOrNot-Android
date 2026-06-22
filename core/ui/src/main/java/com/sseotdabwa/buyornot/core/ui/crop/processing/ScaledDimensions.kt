package com.sseotdabwa.buyornot.core.ui.crop.processing

internal data class ScaledDimensions(
    val width: Int,
    val height: Int,
)

internal fun computeScaledDimensions(
    width: Int,
    height: Int,
    maxDimension: Int,
): ScaledDimensions {
    val longer = maxOf(width, height)
    if (longer <= maxDimension) return ScaledDimensions(width, height)
    val scale = maxDimension.toFloat() / longer
    val newWidth = (width * scale).toInt().coerceAtLeast(1)
    val newHeight = (height * scale).toInt().coerceAtLeast(1)
    return ScaledDimensions(newWidth, newHeight)
}
