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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

private const val MAX_SCALE = 3f

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

    val scaleAnim = remember { Animatable(1f) }
    val offsetAnim =
        remember {
            Animatable(
                initialValue = Offset.Zero,
                typeConverter =
                    TwoWayConverter(
                        convertToVector = { AnimationVector2D(it.x, it.y) },
                        convertFromVector = { Offset(it.v1, it.v2) },
                    ),
            )
        }

    val transformState =
        rememberTransformableState { zoomChange, panChange, _ ->
            val newScale = (scale * zoomChange).coerceIn(1f, MAX_SCALE)
            scale = newScale
            offset = if (newScale > 1f) offset + panChange else Offset.Zero
            onZoomChanged(newScale > 1f)
        }

    // 핀치 제스처 끝난 후 원래 위치로 복귀
    LaunchedEffect(transformState.isTransformInProgress) {
        if (!transformState.isTransformInProgress) {
            if (scale <= 1f) {
                scaleAnim.animateTo(1f, spring())
                offsetAnim.animateTo(Offset.Zero, spring())
                scale = 1f
                offset = Offset.Zero
                onZoomChanged(false)
            } else {
                scaleAnim.snapTo(scale)
                offsetAnim.snapTo(offset)
            }
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .transformable(
                    state = transformState,
                    canPan = { scale > 1f },
                    lockRotationOnZoomPan = true,
                ).pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (scale > 1f) {
                                scale = 1f
                                offset = Offset.Zero
                                onZoomChanged(false)
                            } else {
                                scale = 2f
                                onZoomChanged(true)
                            }
                            scaleAnim.updateBounds(lowerBound = 1f, upperBound = MAX_SCALE)
                        },
                    )
                }.graphicsLayer {
                    scaleX = if (transformState.isTransformInProgress) scale else scaleAnim.value
                    scaleY = if (transformState.isTransformInProgress) scale else scaleAnim.value
                    translationX = if (transformState.isTransformInProgress) offset.x else offsetAnim.value.x
                    translationY = if (transformState.isTransformInProgress) offset.y else offsetAnim.value.y
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
            )
        }
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
