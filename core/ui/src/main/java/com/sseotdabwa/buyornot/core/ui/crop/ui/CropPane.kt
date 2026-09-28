package com.sseotdabwa.buyornot.core.ui.crop.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalDensity
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
    lockedRatio: AspectRatio? = null,
    onPreviewError: (Throwable) -> Unit = {},
) {
    val context = LocalContext.current
    // CropOverlay의 코너 dot indicator가 잘리지 않도록 이미지를 안쪽으로 들이는 여백.
    // imageBounds 계산도 동일한 여백을 사용해 CropOverlay 최대 영역이 실제 이미지 영역과 일치하도록 한다.
    val imageInset = 12.dp
    val imageInsetPx = with(LocalDensity.current) { imageInset.toPx() }
    var tempRatio by remember { mutableStateOf(lockedRatio ?: editSpec.crop?.ratio ?: AspectRatio.Free) }
    var tempRect by remember {
        mutableStateOf(editSpec.crop?.rectNormalized ?: NormalizedRect.Full)
    }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var rotatedBitmap by remember(imageUri, editSpec.rotationQuarters) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(imageUri, editSpec.rotationQuarters) {
        produceEditedPreview(context, imageUri, editSpec.copy(crop = null))
            .onSuccess { rotatedBitmap = it }
            // 실패하면 컨트롤러가 준비되지 않아 확정할 수 없으므로 원인을 화면에 알린다.
            .onFailure(onPreviewError)
    }

    val intrinsicSize: Size =
        rotatedBitmap?.let { Size(it.width.toFloat(), it.height.toFloat()) } ?: Size.Unspecified

    // 화면(픽셀) 기준 비율을 normalized 좌표계로 환산.
    // normalized 1 unit = X축 imageWidth px, Y축 imageHeight px 이므로,
    // 픽셀 ratio (W/H)를 normalized ratio로 변환하려면 imageAspect(=W/H)로 나눠야 한다.
    val normalizedTargetRatio: Float? =
        run {
            val pixelRatio = tempRatio.targetRatio()
            val w = intrinsicSize.width
            val h = intrinsicSize.height
            // 이미지 로드 전 intrinsicSize는 Size.Unspecified(NaN)라 `<= 0f` 비교로는 걸러지지 않는다.
            // NaN 비율이 tempRect에 한 번 들어가면 이후 계산이 모두 NaN이 되므로 양수일 때만 계산한다.
            if (pixelRatio == null || !(w > 0f) || !(h > 0f)) null else pixelRatio * h / w
        }

    LaunchedEffect(normalizedTargetRatio) {
        val ratio = normalizedTargetRatio ?: return@LaunchedEffect
        tempRect = computeRectForRatio(tempRect, NormalizedRect.Full, ratio)
    }

    val imageBounds: Rect? =
        remember(containerSize, intrinsicSize, imageInsetPx) {
            if (containerSize == IntSize.Zero || intrinsicSize == Size.Unspecified) return@remember null
            // 이미지는 inset 여백을 제외한 영역에 Fit으로 그려지므로, 그 영역 기준으로 스케일을 계산한다.
            val availableWidth = containerSize.width - 2 * imageInsetPx
            val availableHeight = containerSize.height - 2 * imageInsetPx
            if (availableWidth <= 0f || availableHeight <= 0f) return@remember null
            val scale = minOf(availableWidth / intrinsicSize.width, availableHeight / intrinsicSize.height)
            val displayedWidth = intrinsicSize.width * scale
            val displayedHeight = intrinsicSize.height * scale
            val left = (containerSize.width - displayedWidth) / 2f
            val top = (containerSize.height - displayedHeight) / 2f
            Rect(left, top, left + displayedWidth, top + displayedHeight)
        }

    // 이미지가 로드되기 전의 tempRect는 비율이 적용되지 않은 Full이다. 위의 비율 보정이 끝난 뒤에
    // 컨트롤러를 넘겨, 로드 전에 확정해 비율이 깨진 crop이 나가는 일을 막는다.
    val isReady = imageBounds != null
    LaunchedEffect(isReady) {
        if (isReady) onControllerReady(CropPaneController(commit = { CropSpec(tempRatio, tempRect) }))
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp)
                    .padding(top = 12.dp)
                    .onSizeChanged { containerSize = it },
            contentAlignment = Alignment.Center,
        ) {
            rotatedBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(imageInset),
                    contentScale = ContentScale.Fit,
                )
            }
            if (imageBounds != null) {
                CropOverlay(
                    cropRect = tempRect,
                    imageBounds = imageBounds,
                    targetRatio = normalizedTargetRatio,
                    onCropRectChange = { tempRect = it },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        // 비율이 고정되면 고를 것이 없으므로 선택 바 대신 여백만 둔다.
        if (lockedRatio == null) {
            CropRatioBar(
                selected = tempRatio,
                onSelect = { newRatio -> tempRatio = newRatio },
            )
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }
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
