package com.sseotdabwa.buyornot.core.ui.crop.state

fun reduce(
    state: EditSpec,
    event: EditEvent,
): EditSpec =
    when (event) {
        is EditEvent.CommitCrop -> state.copy(crop = event.crop)
        is EditEvent.CommitRotate -> {
            val normalized = ((event.quarters % 4) + 4) % 4
            if (normalized == state.rotationQuarters) {
                state
            } else {
                state.copy(rotationQuarters = normalized, crop = null)
            }
        }
    }
