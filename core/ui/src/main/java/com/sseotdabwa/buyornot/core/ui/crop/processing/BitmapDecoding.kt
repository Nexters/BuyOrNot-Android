package com.sseotdabwa.buyornot.core.ui.crop.processing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.exifinterface.media.ExifInterface

/**
 * `uri` 이미지의 longer side가 `maxDimension` 이하가 되도록 디코드할 때 사용할 `inSampleSize` 계산.
 * `inJustDecodeBounds = true` 로 차원만 읽은 뒤 2의 거듭제곱으로 반환한다. (BitmapFactory 권장값)
 */
internal fun computeSampleSize(
    context: Context,
    uri: Uri,
    maxDimension: Int,
): Int {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, options)
    } ?: return 1
    var sampleSize = 1
    val longer = maxOf(options.outWidth, options.outHeight)
    while (longer / sampleSize > maxDimension) {
        sampleSize *= 2
    }
    return sampleSize
}

internal fun decodeBitmap(
    context: Context,
    uri: Uri,
    sampleSize: Int = 1,
): Bitmap {
    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    return context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, options)
    } ?: error("Cannot open image: $uri")
}

internal fun readExifOrientation(
    context: Context,
    uri: Uri,
): Int =
    context.contentResolver.openInputStream(uri)?.use {
        ExifInterface(it).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL,
        )
    } ?: ExifInterface.ORIENTATION_NORMAL
