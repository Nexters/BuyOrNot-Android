package com.sseotdabwa.buyornot.core.ui.crop.processing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import com.sseotdabwa.buyornot.core.ui.crop.state.EditSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

suspend fun processToFile(
    context: Context,
    sourceUri: Uri,
    spec: EditSpec,
): Result<Uri> =
    withContext(Dispatchers.IO) {
        runCatching {
            val raw = decodeBitmap(context, sourceUri)
            val exifOriented = applyExifRotation(raw, readExifOrientation(context, sourceUri))
            val rotated = applyQuarterRotation(exifOriented, spec.rotationQuarters)
            val cropped =
                spec.crop
                    ?.let { cropFromNormalized(rotated, it.rectNormalized) }
                    ?: rotated
            saveJpeg(context, cropped)
        }
    }

private fun decodeBitmap(
    context: Context,
    uri: Uri,
): Bitmap =
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        ?: error("Cannot open image: $uri")

private fun readExifOrientation(
    context: Context,
    uri: Uri,
): Int =
    context.contentResolver.openInputStream(uri)?.use {
        ExifInterface(it).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL,
        )
    } ?: ExifInterface.ORIENTATION_NORMAL

private fun applyQuarterRotation(
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

private fun cropFromNormalized(
    bitmap: Bitmap,
    rect: com.sseotdabwa.buyornot.core.ui.crop.state.NormalizedRect,
): Bitmap {
    val pixel = mapNormalizedToPixel(rect, bitmap.width, bitmap.height)
    val cropped = Bitmap.createBitmap(bitmap, pixel.srcX, pixel.srcY, pixel.srcW, pixel.srcH)
    bitmap.recycle()
    return cropped
}

private fun saveJpeg(
    context: Context,
    bitmap: Bitmap,
): Uri {
    val file = File(context.cacheDir, "edit_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
    bitmap.recycle()
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
}
