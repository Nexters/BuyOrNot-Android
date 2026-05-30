package com.sseotdabwa.buyornot.core.ui.crop.state

enum class AspectRatio {
    Free,
    R1x1,
    R3x4,
    R4x3,
    ;

    fun targetRatio(): Float? =
        when (this) {
            Free -> null
            R1x1 -> 1f
            R3x4 -> 0.75f
            R4x3 -> 4f / 3f
        }
}
