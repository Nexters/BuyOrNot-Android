package com.sseotdabwa.buyornot.core.ui.crop.state

sealed interface EditEvent {
    data class CommitCrop(
        val crop: CropSpec,
    ) : EditEvent

    data class CommitRotate(
        val quarters: Int,
    ) : EditEvent
}
