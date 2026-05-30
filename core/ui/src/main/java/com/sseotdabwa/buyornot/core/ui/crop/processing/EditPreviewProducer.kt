package com.sseotdabwa.buyornot.core.ui.crop.processing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.sseotdabwa.buyornot.core.ui.crop.state.EditSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val PREVIEW_MAX_DIMENSION = 1024

private data class BaseBitmapCache(
    val uri: String,
    val bitmap: Bitmap,
)

private var baseBitmapCache: BaseBitmapCache? = null
private val baseBitmapLock = Any()

/**
 * 편집된 이미지의 미리보기 비트맵을 생성한다. `processToFile`과 동일한 변환 파이프라인을
 * 적용하되, 디스크에 쓰지 않고 메모리 비트맵으로 반환한다. 메모리 사용을 제한하기 위해
 * 디코드 시점에 다운샘플링한다 (longer side ≲ 1024px).
 *
 * 동일한 `sourceUri`에 대한 디코드+EXIF 결과 비트맵은 메모리에 캐싱되어, 회전·자르기 spec
 * 변경 시 디스크 디코드를 반복하지 않는다. URI가 바뀌면 이전 캐시는 recycle되고 새로 채워진다.
 */
suspend fun produceEditedPreview(
    context: Context,
    sourceUri: Uri,
    spec: EditSpec,
): Result<Bitmap> =
    withContext(Dispatchers.IO) {
        runCatching {
            val base = getOrDecodeBase(context, sourceUri)
            val rotated = rotateWithoutRecycle(base, spec.rotationQuarters)
            spec.crop?.let { cropSpec ->
                val pixel = mapNormalizedToPixel(cropSpec.rectNormalized, rotated.width, rotated.height)
                val cropped = Bitmap.createBitmap(rotated, pixel.srcX, pixel.srcY, pixel.srcW, pixel.srcH)
                if (rotated !== base) rotated.recycle()
                cropped
            } ?: rotated
        }
    }

private fun getOrDecodeBase(
    context: Context,
    sourceUri: Uri,
): Bitmap {
    val uriKey = sourceUri.toString()
    synchronized(baseBitmapLock) {
        val cached = baseBitmapCache
        if (cached != null && cached.uri == uriKey && !cached.bitmap.isRecycled) {
            return cached.bitmap
        }
    }
    val sampleSize = computePreviewSampleSize(context, sourceUri)
    val raw = decodeBitmap(context, sourceUri, sampleSize)
    val oriented = applyExifRotation(raw, readExifOrientation(context, sourceUri))
    synchronized(baseBitmapLock) {
        val previous = baseBitmapCache
        if (previous != null && previous.uri != uriKey && !previous.bitmap.isRecycled) {
            previous.bitmap.recycle()
        }
        baseBitmapCache = BaseBitmapCache(uriKey, oriented)
    }
    return oriented
}

private fun rotateWithoutRecycle(
    bitmap: Bitmap,
    quarters: Int,
): Bitmap {
    val normalized = ((quarters % 4) + 4) % 4
    if (normalized == 0) return bitmap
    val matrix = Matrix().apply { postRotate(-90f * normalized) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
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
