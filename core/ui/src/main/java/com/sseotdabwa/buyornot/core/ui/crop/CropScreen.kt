package com.sseotdabwa.buyornot.core.ui.crop

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun CropScreen(
    imageUri: Uri,
    onConfirm: (Uri) -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var intrinsicSize by remember { mutableStateOf(Size.Unspecified) }
    var isExtracting by remember { mutableStateOf(false) }

    val imageBounds: Rect? =
        remember(containerSize, intrinsicSize) {
            if (containerSize == IntSize.Zero || intrinsicSize == Size.Unspecified) return@remember null
            val scale =
                minOf(
                    containerSize.width / intrinsicSize.width,
                    containerSize.height / intrinsicSize.height,
                )
            val displayedWidth = intrinsicSize.width * scale
            val displayedHeight = intrinsicSize.height * scale
            val left = (containerSize.width - displayedWidth) / 2f
            val top = (containerSize.height - displayedHeight) / 2f
            Rect(left, top, left + displayedWidth, top + displayedHeight)
        }

    var cropRect by remember(imageBounds) {
        mutableStateOf(
            imageBounds?.let { bounds ->
                val size = minOf(bounds.width, bounds.height)
                val left = bounds.left + (bounds.width - size) / 2f
                val top = bounds.top + (bounds.height - size) / 2f
                Rect(left, top, left + size, top + size)
            } ?: Rect.Zero,
        )
    }

    Scaffold(
        topBar = {
            CropTopBar(
                onCancel = onCancel,
                onConfirm = {
                    val capturedBounds = imageBounds ?: return@CropTopBar
                    isExtracting = true
                    scope.launch {
                        val result =
                            withContext(Dispatchers.IO) {
                                runCatching {
                                    val bitmap =
                                        context.contentResolver
                                            .openInputStream(imageUri)
                                            ?.use { BitmapFactory.decodeStream(it) }
                                            ?: error("Cannot open image")
                                    val bounds = capturedBounds
                                    val scaleX = bitmap.width / bounds.width
                                    val scaleY = bitmap.height / bounds.height
                                    val srcX = ((cropRect.left - bounds.left) * scaleX).toInt().coerceAtLeast(0)
                                    val srcY = ((cropRect.top - bounds.top) * scaleY).toInt().coerceAtLeast(0)
                                    val srcW = (cropRect.width * scaleX).toInt().coerceAtMost(bitmap.width - srcX)
                                    val srcH = (cropRect.height * scaleY).toInt().coerceAtMost(bitmap.height - srcY)
                                    val cropped = Bitmap.createBitmap(bitmap, srcX, srcY, srcW, srcH)
                                    bitmap.recycle()
                                    val file = File(context.cacheDir, "crop_${System.currentTimeMillis()}.jpg")
                                    FileOutputStream(file).use {
                                        cropped.compress(Bitmap.CompressFormat.JPEG, 90, it)
                                    }
                                    cropped.recycle()
                                    FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file,
                                    )
                                }
                            }
                        isExtracting = false
                        result.onSuccess { onConfirm(it) }
                        result.onFailure { onCancel() }
                    }
                },
                isConfirmEnabled = !isExtracting && imageBounds != null,
            )
        },
        containerColor = Color.Black,
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .onSizeChanged { containerSize = it },
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
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
                    cropRect = cropRect,
                    imageBounds = imageBounds,
                    onCropRectChange = { cropRect = it },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            if (isExtracting) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun CropTopBar(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    isConfirmEnabled: Boolean,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .height(60.dp)
                .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onCancel) {
            Icon(
                imageVector = BuyOrNotIcons.Close.asImageVector(),
                contentDescription = "취소",
                tint = Color.White,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = onConfirm,
            enabled = isConfirmEnabled,
        ) {
            Icon(
                imageVector = BuyOrNotIcons.Check.asImageVector(),
                contentDescription = "확인",
                tint = if (isConfirmEnabled) Color.White else Color.White.copy(alpha = 0.3f),
            )
        }
    }
}
