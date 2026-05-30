package com.sseotdabwa.buyornot.core.ui.crop.processing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import com.sseotdabwa.buyornot.core.ui.crop.state.EditSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

private const val MAX_OUTPUT_DIMENSION = 1024

suspend fun processToFile(
    context: Context,
    sourceUri: Uri,
    spec: EditSpec,
): Result<Uri> =
    withContext(Dispatchers.IO) {
        runCatching {
            val sampleSize = computeSampleSize(context, sourceUri, MAX_OUTPUT_DIMENSION)
            val raw = decodeBitmap(context, sourceUri, sampleSize)
            val exifOriented = applyExifRotation(raw, readExifOrientation(context, sourceUri))
            val rotated = applyQuarterRotation(exifOriented, spec.rotationQuarters)
            val cropped =
                spec.crop
                    ?.let { cropFromNormalized(rotated, it.rectNormalized) }
                    ?: rotated
            val resized = downscaleToMaxDimension(cropped, MAX_OUTPUT_DIMENSION)
            saveJpeg(context, resized)
        }
    }

internal fun applyQuarterRotation(
    bitmap: Bitmap,
    quarters: Int,
): Bitmap {
    val normalized = ((quarters % 4) + 4) % 4
    if (normalized == 0) return bitmap
    val matrix = Matrix().apply { postRotate(-90f * normalized) }
    val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    bitmap.recycle()
    return rotated
}

internal fun cropFromNormalized(
    bitmap: Bitmap,
    rect: com.sseotdabwa.buyornot.core.ui.crop.state.NormalizedRect,
): Bitmap {
    val pixel = mapNormalizedToPixel(rect, bitmap.width, bitmap.height)
    val cropped = Bitmap.createBitmap(bitmap, pixel.srcX, pixel.srcY, pixel.srcW, pixel.srcH)
    bitmap.recycle()
    return cropped
}

private fun downscaleToMaxDimension(
    bitmap: Bitmap,
    maxDimension: Int,
): Bitmap {
    val target = computeScaledDimensions(bitmap.width, bitmap.height, maxDimension)
    if (target.width == bitmap.width && target.height == bitmap.height) return bitmap
    val scaled = Bitmap.createScaledBitmap(bitmap, target.width, target.height, true)
    if (scaled !== bitmap) bitmap.recycle()
    return scaled
}

private const val CROPPED_IMAGES_DIR = "cropped_images"

private fun saveJpeg(
    context: Context,
    bitmap: Bitmap,
): Uri {
    val dir = File(context.cacheDir, CROPPED_IMAGES_DIR).apply { mkdirs() }
    val file = File(dir, "edit_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
    bitmap.recycle()
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
}
