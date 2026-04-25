package com.sseotdabwa.buyornot.core.ui.imageviewer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private const val MAX_SCALE = 3f

internal fun computeMaxOffset(
    containerWidth: Int,
    containerHeight: Int,
    imageWidth: Float,
    imageHeight: Float,
    scale: Float,
): Pair<Float, Float>? {
    if (containerWidth == 0 ||
        containerHeight == 0 ||
        !imageWidth.isFinite() ||
        !imageHeight.isFinite() ||
        imageWidth <= 0f ||
        imageHeight <= 0f
    ) {
        return null
    }
    val ratio = minOf(containerWidth.toFloat() / imageWidth, containerHeight.toFloat() / imageHeight)
    val maxX = maxOf(0f, (imageWidth * ratio * scale - containerWidth) / 2)
    val maxY = maxOf(0f, (imageHeight * ratio * scale - containerHeight) / 2)
    return Pair(maxX, maxY)
}

internal fun computeFocalOffset(
    currentOffset: Offset,
    centroid: Offset,
    containerWidth: Int,
    containerHeight: Int,
    currentScale: Float,
    newScale: Float,
    pan: Offset,
): Offset {
    if (newScale <= 1f) return Offset.Zero
    val actualZoom = if (currentScale > 0f) newScale / currentScale else newScale
    val centroidFromCenter = centroid - Offset(containerWidth / 2f, containerHeight / 2f)
    return centroidFromCenter * (1 - actualZoom) + currentOffset * actualZoom + pan
}

@Composable
fun ImageViewerScreen(
    imageUrls: List<String>,
    initialPage: Int,
    onBackClick: () -> Unit,
) {
    val pagerState =
        rememberPagerState(
            initialPage = initialPage,
            pageCount = { imageUrls.size },
        )
    var isZoomed by remember { mutableStateOf(false) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(60.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 10.dp, top = 10.dp)
                        .size(40.dp)
                        .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = BuyOrNotIcons.Close.asImageVector(),
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().weight(1f),
            userScrollEnabled = !isZoomed,
        ) { page ->
            ZoomableImage(
                imageUrl = imageUrls[page],
                onZoomChanged = { zoomed -> isZoomed = zoomed },
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(60.dp),
        )
    }
}

@Composable
private fun ZoomableImage(
    imageUrl: String,
    onZoomChanged: (Boolean) -> Unit,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var imageIntrinsicSize by remember { mutableStateOf(Size.Unspecified) }
    var hasPinched by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val transformState =
        rememberTransformableState { zoomChange, panChange, _ ->
            if (zoomChange != 1f) hasPinched = true
            val newScale = (scale * zoomChange).coerceIn(1f, MAX_SCALE)
            scale = newScale
            val rawOffset = if (newScale > 1f) offset + panChange else Offset.Zero
            offset = computeMaxOffset(
                containerWidth = containerSize.width,
                containerHeight = containerSize.height,
                imageWidth = imageIntrinsicSize.width,
                imageHeight = imageIntrinsicSize.height,
                scale = newScale,
            )?.let { (maxX, maxY) ->
                Offset(rawOffset.x.coerceIn(-maxX, maxX), rawOffset.y.coerceIn(-maxY, maxY))
            } ?: rawOffset
            onZoomChanged(newScale > 1f)
        }

    LaunchedEffect(transformState.isTransformInProgress) {
        if (!transformState.isTransformInProgress && hasPinched) {
            hasPinched = false
            animateResetZoom(
                currentScale = scale,
                currentOffset = offset,
                onScaleChange = { scale = it },
                onOffsetChange = { offset = it },
            )
            onZoomChanged(false)
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .onSizeChanged { containerSize = it }
                .transformable(
                    state = transformState,
                    canPan = { scale > 1f },
                    lockRotationOnZoomPan = true,
                ).pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            scope.launch {
                                if (scale > 1f) {
                                    animateResetZoom(
                                        currentScale = scale,
                                        currentOffset = offset,
                                        onScaleChange = { scale = it },
                                        onOffsetChange = { offset = it },
                                    )
                                    onZoomChanged(false)
                                } else {
                                    scale = 2f
                                    offset = Offset.Zero
                                    onZoomChanged(true)
                                }
                            }
                        },
                    )
                }.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
        contentAlignment = Alignment.Center,
    ) {
        val isPreview = LocalInspectionMode.current
        if (isPreview) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.White),
            )
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                onSuccess = { state ->
                    val drawable = state.result.drawable
                    imageIntrinsicSize =
                        Size(
                            drawable.intrinsicWidth.toFloat(),
                            drawable.intrinsicHeight.toFloat(),
                        )
                },
            )
        }
    }
}

private suspend fun animateResetZoom(
    currentScale: Float,
    currentOffset: Offset,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Offset) -> Unit,
) {
    coroutineScope {
        val scaleAnim = Animatable(currentScale)
        val offsetAnim =
            Animatable(
                initialValue = currentOffset,
                typeConverter =
                    TwoWayConverter(
                        convertToVector = { AnimationVector2D(it.x, it.y) },
                        convertFromVector = { Offset(it.v1, it.v2) },
                    ),
            )
        launch { scaleAnim.animateTo(1f, spring()) { onScaleChange(value) } }
        launch { offsetAnim.animateTo(Offset.Zero, spring()) { onOffsetChange(value) } }
    }
}

@Preview(showBackground = true)
@Composable
private fun ImageViewerScreenPreview() {
    BuyOrNotTheme {
        ImageViewerScreen(
            imageUrls = listOf("url1", "url2", "url3"),
            initialPage = 0,
            onBackClick = {},
        )
    }
}

@Preview(showBackground = true, name = "ImageViewerScreen - 2페이지")
@Composable
private fun ImageViewerScreenSecondPagePreview() {
    BuyOrNotTheme {
        ImageViewerScreen(
            imageUrls = listOf("url1", "url2", "url3"),
            initialPage = 1,
            onBackClick = {},
        )
    }
}
