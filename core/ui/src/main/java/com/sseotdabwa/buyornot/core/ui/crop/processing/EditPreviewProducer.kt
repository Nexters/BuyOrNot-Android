package com.sseotdabwa.buyornot.core.ui.crop.processing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import com.sseotdabwa.buyornot.core.ui.crop.state.EditSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val PREVIEW_MAX_DIMENSION = 1024

/**
 * 편집된 이미지의 미리보기 비트맵을 생성한다. `processToFile`과 동일한 변환 파이프라인을
 * 적용하되, 디스크에 쓰지 않고 메모리 비트맵으로 반환한다. 메모리 사용을 제한하기 위해
 * 디코드 시점에 다운샘플링한다 (longer side ≲ 1024px).
 */
suspend fun produceEditedPreview(
    context: Context,
    sourceUri: Uri,
    spec: EditSpec,
): Result<Bitmap> =
    withContext(Dispatchers.IO) {
        runCatching {
            val sampleSize = computePreviewSampleSize(context, sourceUri)
            val raw = decodeBitmap(context, sourceUri, sampleSize)
            val exifOriented = applyExifRotation(raw, readExifOrientation(context, sourceUri))
            val rotated = applyQuarterRotation(exifOriented, spec.rotationQuarters)
            spec.crop?.let { cropFromNormalized(rotated, it.rectNormalized) } ?: rotated
        }
    }

private fun computePreviewSampleSize(
    context: Context,
    uri: Uri,
): Int {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, options)
    } ?: return 1
    var sampleSize = 1
    val longer = maxOf(options.outWidth, options.outHeight)
    while (longer / sampleSize > PREVIEW_MAX_DIMENSION) {
        sampleSize *= 2
    }
    return sampleSize
}

private fun decodeBitmap(
    context: Context,
    uri: Uri,
    sampleSize: Int,
): Bitmap {
    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    return context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, options)
    } ?: error("Cannot open image: $uri")
}

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
