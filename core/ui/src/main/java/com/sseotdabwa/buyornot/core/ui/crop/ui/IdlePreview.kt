package com.sseotdabwa.buyornot.core.ui.crop.ui

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import coil.compose.AsyncImage
import com.sseotdabwa.buyornot.core.ui.crop.state.EditSpec

@Composable
internal fun IdlePreview(
    imageUri: Uri,
    editSpec: EditSpec,
    modifier: Modifier = Modifier,
) {
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var intrinsicSize by remember { mutableStateOf(Size.Unspecified) }

    Box(
        modifier =
            modifier
                .fillMaxSize()
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

        val crop = editSpec.crop
        if (crop != null && containerSize != IntSize.Zero && intrinsicSize != Size.Unspecified) {
            val bounds = computeRotatedBounds(containerSize, intrinsicSize, editSpec.rotationQuarters)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val rectPx =
                    Rect(
                        left = bounds.left + crop.rectNormalized.left * bounds.width,
                        top = bounds.top + crop.rectNormalized.top * bounds.height,
                        right = bounds.left + crop.rectNormalized.right * bounds.width,
                        bottom = bounds.top + crop.rectNormalized.bottom * bounds.height,
                    )
                val maskColor = Color.Black.copy(alpha = 0.5f)
                drawRect(maskColor, topLeft = Offset.Zero, size = Size(size.width, rectPx.top))
                drawRect(maskColor, topLeft = Offset(0f, rectPx.bottom), size = Size(size.width, size.height - rectPx.bottom))
                drawRect(maskColor, topLeft = Offset(0f, rectPx.top), size = Size(rectPx.left, rectPx.height))
                drawRect(maskColor, topLeft = Offset(rectPx.right, rectPx.top), size = Size(size.width - rectPx.right, rectPx.height))
            }
        }
    }
}

private fun computeRotatedBounds(
    container: IntSize,
    intrinsic: Size,
    quarters: Int,
): Rect {
    val effectiveWidth = if (quarters % 2 == 0) intrinsic.width else intrinsic.height
    val effectiveHeight = if (quarters % 2 == 0) intrinsic.height else intrinsic.width
    val scale = minOf(container.width / effectiveWidth, container.height / effectiveHeight)
    val displayedWidth = effectiveWidth * scale
    val displayedHeight = effectiveHeight * scale
    val left = (container.width - displayedWidth) / 2f
    val top = (container.height - displayedHeight) / 2f
    return Rect(left, top, left + displayedWidth, top + displayedHeight)
}
