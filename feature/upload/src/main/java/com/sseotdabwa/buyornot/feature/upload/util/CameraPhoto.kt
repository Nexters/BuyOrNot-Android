package com.sseotdabwa.buyornot.feature.upload.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

/**
 * 카메라 앱이 찍은 사진을 저장할 갤러리 항목을 만든다. 촬영을 취소하면 [Context.contentResolver]로 지워야 한다.
 *
 * @return 만들 수 없으면 null. Android 10 미만은 공용 저장소 쓰기 권한이 없으면 거절된다.
 */
internal fun createCameraPhotoUri(context: Context): Uri? {
    val contentValues =
        ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "buyornot_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/BuyOrNot")
            }
        }
    return runCatching { context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) }.getOrNull()
}
