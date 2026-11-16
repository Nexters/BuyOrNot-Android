package com.sseotdabwa.buyornot.feature.upload.album

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class GalleryPhoto(
    val uri: Uri,
    val bucketId: Long,
)

data class GalleryBucket(
    val id: Long,
    val name: String,
)

data class Gallery(
    val photos: List<GalleryPhoto>,
    val buckets: List<GalleryBucket>,
)

/** 기기 사진을 최근에 추가한 순서로 읽는다. 읽기 권한이 있어야 하고, 일부만 허용됐으면 허용된 사진만 나온다. */
class GallerySource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun load(): Gallery =
        withContext(Dispatchers.IO) {
            val projection =
                arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                )
            val photos = mutableListOf<GalleryPhoto>()
            val buckets = LinkedHashMap<Long, GalleryBucket>()
            context.contentResolver
                .query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    // 촬영을 취소하고 지우지 못한 빈 항목은 열 수 없어 고르지 못하게 뺀다.
                    "${MediaStore.Images.Media.SIZE} > 0",
                    null,
                    "${MediaStore.Images.Media.DATE_ADDED} DESC",
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
                    val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                    while (cursor.moveToNext()) {
                        val bucketId = cursor.getLong(bucketIdColumn)
                        val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getLong(idColumn))
                        photos += GalleryPhoto(uri = uri, bucketId = bucketId)
                        if (bucketId !in buckets) {
                            buckets[bucketId] = GalleryBucket(id = bucketId, name = cursor.getString(bucketNameColumn).orEmpty())
                        }
                    }
                }
            Gallery(photos = photos, buckets = buckets.values.toList())
        }
}
