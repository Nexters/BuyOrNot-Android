package com.sseotdabwa.buyornot.core.ui.crop.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.ui.crop.CropOverlay
import com.sseotdabwa.buyornot.core.ui.crop.geometry.computeRectForRatio
import com.sseotdabwa.buyornot.core.ui.crop.processing.produceEditedPreview
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
    val context = LocalContext.current
    var tempRatio by remember { mutableStateOf(editSpec.crop?.ratio ?: AspectRatio.Free) }
    var tempRect by remember {
        mutableStateOf(editSpec.crop?.rectNormalized ?: NormalizedRect.Full)
    }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var rotatedBitmap by remember(imageUri, editSpec.rotationQuarters) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(imageUri, editSpec.rotationQuarters) {
        produceEditedPreview(context, imageUri, editSpec.copy(crop = null))
            .onSuccess { rotatedBitmap = it }
    }

    val intrinsicSize: Size =
        rotatedBitmap?.let { Size(it.width.toFloat(), it.height.toFloat()) } ?: Size.Unspecified

    val imageBounds: Rect? =
        remember(containerSize, intrinsicSize) {
            if (containerSize == IntSize.Zero || intrinsicSize == Size.Unspecified) return@remember null
            val scale = minOf(containerSize.width / intrinsicSize.width, containerSize.height / intrinsicSize.height)
            val displayedWidth = intrinsicSize.width * scale
            val displayedHeight = intrinsicSize.height * scale
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
                    .padding(horizontal = 20.dp)
                    .onSizeChanged { containerSize = it },
            contentAlignment = Alignment.Center,
        ) {
            rotatedBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
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

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 375, heightDp = 812)
@Composable
private fun CropPaneFreePreview() {
    CropPane(
        imageUri = Uri.EMPTY,
        editSpec = EditSpec(crop = CropSpec(AspectRatio.Free, NormalizedRect.Full)),
        onControllerReady = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 375, heightDp = 812)
@Composable
private fun CropPane1x1Preview() {
    CropPane(
        imageUri = Uri.EMPTY,
        editSpec = EditSpec(crop = CropSpec(AspectRatio.R1x1, NormalizedRect(0.1f, 0.1f, 0.7f, 0.7f))),
        onControllerReady = {},
    )
}
