package com.sseotdabwa.buyornot.core.ui.crop.processing

data class PixelRect(
    val srcX: Int,
    val srcY: Int,
    val srcW: Int,
    val srcH: Int,
) {
    /** 짧은 변에 맞춰 가운데를 남긴 정사각형. 정규화 좌표 반올림으로 생기는 1px 오차를 없앤다. */
    fun squared(): PixelRect {
        val side = minOf(srcW, srcH)
        return PixelRect(
            srcX = srcX + (srcW - side) / 2,
            srcY = srcY + (srcH - side) / 2,
            srcW = side,
            srcH = side,
        )
    }
}
