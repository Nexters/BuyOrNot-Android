package com.sseotdabwa.buyornot.domain.repository

import com.sseotdabwa.buyornot.domain.model.FeedCategory
import com.sseotdabwa.buyornot.domain.model.UploadInfo

interface FeedRepository {
    suspend fun getPresignedUrl(
        fileName: String,
        contentType: String,
    ): UploadInfo

    suspend fun uploadImage(
        url: String,
        bytes: ByteArray,
        contentType: String,
    )

    suspend fun createFeed(
        category: FeedCategory,
        price: Int,
        content: String,
        s3ObjectKey: String,
        imageWidth: Int,
        imageHeight: Int,
    ): Long
}
