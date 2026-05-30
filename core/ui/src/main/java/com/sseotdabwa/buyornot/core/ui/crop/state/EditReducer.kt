package com.sseotdabwa.buyornot.core.ui.crop.state

import com.sseotdabwa.buyornot.core.ui.crop.geometry.rotateCounterClockwise

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
                val deltaQuarters = (normalized - state.rotationQuarters + 4) % 4
                val newCrop =
                    state.crop?.let { existing ->
                        existing.copy(
                            rectNormalized = existing.rectNormalized.rotateCounterClockwise(deltaQuarters),
                        )
                    }
                state.copy(rotationQuarters = normalized, crop = newCrop)
            }
        }
    }
