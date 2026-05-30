package com.sseotdabwa.buyornot.core.ui.crop.ui

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import coil.compose.AsyncImage
import com.sseotdabwa.buyornot.core.ui.crop.CropOverlay
import com.sseotdabwa.buyornot.core.ui.crop.geometry.computeRectForRatio
import com.sseotdabwa.buyornot.core.ui.crop.state.AspectRatio
import com.sseotdabwa.buyornot.core.ui.crop.state.CropSpec
import com.sseotdabwa.buyornot.core.ui.crop.state.EditSpec
import com.sseotdabwa.buyornot.core.ui.crop.state.NormalizedRect

internal data class CropPaneController(
    val commit: () -> CropSpec,
)

@Composable
internal fun CropPane(
    imageUri: Uri,
    editSpec: EditSpec,
    onControllerReady: (CropPaneController) -> Unit,
    modifier: Modifier = Modifier,
) {
    var tempRatio by remember { mutableStateOf(editSpec.crop?.ratio ?: AspectRatio.Free) }
    var tempRect by remember {
        mutableStateOf(editSpec.crop?.rectNormalized ?: NormalizedRect.Full)
    }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var intrinsicSize by remember { mutableStateOf(Size.Unspecified) }

    val imageBounds: Rect? =
        remember(containerSize, intrinsicSize, editSpec.rotationQuarters) {
            if (containerSize == IntSize.Zero || intrinsicSize == Size.Unspecified) return@remember null
            val q = editSpec.rotationQuarters
            val effectiveWidth = if (q % 2 == 0) intrinsicSize.width else intrinsicSize.height
            val effectiveHeight = if (q % 2 == 0) intrinsicSize.height else intrinsicSize.width
            val scale = minOf(containerSize.width / effectiveWidth, containerSize.height / effectiveHeight)
            val displayedWidth = effectiveWidth * scale
            val displayedHeight = effectiveHeight * scale
            val left = (containerSize.width - displayedWidth) / 2f
            val top = (containerSize.height - displayedHeight) / 2f
            Rect(left, top, left + displayedWidth, top + displayedHeight)
        }

    DisposableEffect(Unit) {
        onControllerReady(CropPaneController(commit = { CropSpec(tempRatio, tempRect) }))
        onDispose { }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .onSizeChanged { containerSize = it },
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationZ = -90f * editSpec.rotationQuarters },
                contentScale = ContentScale.Fit,
                onSuccess = { state ->
                    intrinsicSize =
                        Size(
                            state.result.drawable.intrinsicWidth
                                .toFloat(),
                            state.result.drawable.intrinsicHeight
                                .toFloat(),
                        )
                },
            )
            if (imageBounds != null) {
                CropOverlay(
                    cropRect = tempRect,
                    imageBounds = imageBounds,
                    targetRatio = tempRatio.targetRatio(),
                    onCropRectChange = { tempRect = it },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        CropRatioBar(
            selected = tempRatio,
            onSelect = { newRatio ->
                tempRatio = newRatio
                tempRect = computeRectForRatio(tempRect, NormalizedRect.Full, newRatio.targetRatio())
            },
        )
    }
}
