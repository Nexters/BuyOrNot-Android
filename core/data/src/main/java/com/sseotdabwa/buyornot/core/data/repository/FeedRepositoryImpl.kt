package com.sseotdabwa.buyornot.core.data.repository

import com.sseotdabwa.buyornot.core.network.api.FeedApiService
import com.sseotdabwa.buyornot.core.network.dto.request.FeedRequest
import com.sseotdabwa.buyornot.core.network.dto.request.PresignedUrlRequest
import com.sseotdabwa.buyornot.core.network.dto.response.getOrThrow
import com.sseotdabwa.buyornot.domain.model.FeedCategory
import com.sseotdabwa.buyornot.domain.model.UploadInfo
import com.sseotdabwa.buyornot.domain.repository.FeedRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val feedApiService: FeedApiService,
) : FeedRepository {
    override suspend fun getPresignedUrl(
        fileName: String,
        contentType: String,
    ): UploadInfo {
        val response =
            feedApiService
                .getPresignedUrl(
                    PresignedUrlRequest(
                        fileName = fileName,
                        contentType = contentType,
                    ),
                ).getOrThrow()

        return UploadInfo(
            uploadUrl = response.uploadUrl,
            s3ObjectKey = response.s3ObjectKey,
            viewUrl = response.viewUrl,
        )
    }

    override suspend fun uploadImage(
        url: String,
        bytes: ByteArray,
        contentType: String,
    ) {
        // ByteArray를 S3 업로드를 위한 RequestBody로 변환
        val requestBody = bytes.toRequestBody(contentType.toMediaTypeOrNull())
        val response = feedApiService.uploadImage(url, contentType, requestBody)
        if (!response.isSuccessful) {
            throw Exception("S3 업로드 실패: ${response.code()}")
        }
    }

    override suspend fun createFeed(
        category: FeedCategory,
        price: Int,
        content: String,
        s3ObjectKey: String,
        imageWidth: Int,
        imageHeight: Int,
    ): Long =
        feedApiService
            .createFeed(
                FeedRequest(
                    category = category.name,
                    price = price,
                    content = content,
                    s3ObjectKey = s3ObjectKey,
                    imageWidth = imageWidth,
                    imageHeight = imageHeight,
                ),
            ).getOrThrow()
            .feedId
}
