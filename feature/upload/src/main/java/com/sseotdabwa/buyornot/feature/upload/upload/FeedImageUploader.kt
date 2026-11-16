package com.sseotdabwa.buyornot.feature.upload.upload

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.sseotdabwa.buyornot.domain.model.FeedImage
import com.sseotdabwa.buyornot.domain.model.ImageSource
import com.sseotdabwa.buyornot.domain.repository.FeedRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UnreadableImageException(
    uri: Uri,
) : IllegalStateException("이미지를 읽을 수 없습니다: $uri")

private data class LocalImage(
    val width: Int,
    val height: Int,
    val bytes: ByteArray,
    val contentType: String,
    val fileName: String,
)

interface FeedImageUploader {
    /**
     * 기기 사진을 presigned URL로 올리고 피드 등록·AI 초안에 쓸 key를 돌려준다.
     *
     * @throws UnreadableImageException 사진을 열 수 없거나 크기를 읽지 못했을 때.
     */
    suspend fun upload(uri: Uri): FeedImage
}

internal class ContentResolverFeedImageUploader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val feedRepository: FeedRepository,
) : FeedImageUploader {
    override suspend fun upload(uri: Uri): FeedImage {
        val image =
            withContext(Dispatchers.IO) {
                val (width, height) = readDimensions(uri)
                if (width <= 0 || height <= 0) throw UnreadableImageException(uri)
                LocalImage(
                    width = width,
                    height = height,
                    bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: throw UnreadableImageException(uri),
                    contentType = contentType(uri) ?: throw UnreadableImageException(uri),
                    fileName = fileName(uri) ?: "upload_image.jpg",
                )
            }
        val (width, height, bytes, contentType) = image
        val uploadInfo = feedRepository.getPresignedUrl(image.fileName, contentType)
        feedRepository.uploadImage(uploadInfo.uploadUrl, bytes, contentType)
        return FeedImage(
            s3ObjectKey = uploadInfo.s3ObjectKey,
            image = ImageSource(uploadInfo.viewUrl),
            imageWidth = width,
            imageHeight = height,
        )
    }

    // 형식을 모르는 사진을 JPEG로 올리면 서버가 잘못된 형식으로 저장하므로, 확장자로도 못 정하면 읽지 못한 것으로 본다.
    private fun contentType(uri: Uri): String? =
        context.contentResolver.getType(uri)
            ?: MimeTypeMap.getFileExtensionFromUrl(uri.toString())?.lowercase()?.let {
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(it)
            }

    private fun fileName(uri: Uri): String? =
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && index != -1) cursor.getString(index) else null
        }

    private fun readDimensions(uri: Uri): Pair<Int, Int> =
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(input, null, options)
                options.outWidth to options.outHeight
            }
        }.getOrNull() ?: (0 to 0)
}
