package com.sseotdabwa.buyornot.core.ui.crop.state

data class NormalizedRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top

    companion object {
        val Full = NormalizedRect(0f, 0f, 1f, 1f)
    }
}

data class CropSpec(
    val ratio: AspectRatio,
    val rectNormalized: NormalizedRect,
)

data class EditSpec(
    val rotationQuarters: Int = 0,
    val crop: CropSpec? = null,
)
