package com.sseotdabwa.buyornot.core.data.repository

import com.sseotdabwa.buyornot.core.network.api.FeedApiService
import com.sseotdabwa.buyornot.core.network.dto.request.FeedRequest
import com.sseotdabwa.buyornot.core.network.dto.request.PresignedUrlRequest
import com.sseotdabwa.buyornot.core.network.dto.request.VoteRequest
import com.sseotdabwa.buyornot.core.network.dto.response.AuthorDto
import com.sseotdabwa.buyornot.core.network.dto.response.FeedItemDto
import com.sseotdabwa.buyornot.core.network.dto.response.VoteResponse
import com.sseotdabwa.buyornot.core.network.dto.response.getOrThrow
import com.sseotdabwa.buyornot.domain.model.Author
import com.sseotdabwa.buyornot.domain.model.Feed
import com.sseotdabwa.buyornot.domain.model.FeedCategory
import com.sseotdabwa.buyornot.domain.model.FeedStatus
import com.sseotdabwa.buyornot.domain.model.UploadInfo
import com.sseotdabwa.buyornot.domain.model.VoteChoice
import com.sseotdabwa.buyornot.domain.model.VoteResult
import com.sseotdabwa.buyornot.domain.repository.FeedRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val feedApiService: FeedApiService,
) : FeedRepository {
    override suspend fun getFeedList(
        cursor: Long?,
        size: Int,
        feedStatus: String?,
    ): List<Feed> =
        feedApiService
            .getFeedList(cursor, size, feedStatus)
            .getOrThrow()
            .content
            .map { it.toDomain() }

    override suspend fun getFeed(feedId: Long): Feed =
        feedApiService
            .getFeed(feedId)
            .getOrThrow()
            .toDomain()

    override suspend fun getMyFeeds(
        cursor: Long?,
        size: Int,
        feedStatus: String?,
    ): List<Feed> =
        feedApiService
            .getMyFeeds(cursor, size, feedStatus)
            .getOrThrow()
            .content
            .map { it.toDomain() }

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

    override suspend fun deleteFeed(feedId: Long) {
        feedApiService.deleteFeed(feedId).getOrThrow()
    }

    override suspend fun reportFeed(feedId: Long) {
        feedApiService.reportFeed(feedId).getOrThrow()
    }

    override suspend fun voteFeed(
        feedId: Long,
        choice: VoteChoice,
    ): VoteResult {
        val response =
            feedApiService
                .voteFeed(
                    feedId = feedId,
                    request = VoteRequest(choice = choice.name),
                ).getOrThrow()
        return response.toDomain()
    }

    override suspend fun voteGuestFeed(
        feedId: Long,
        choice: VoteChoice,
    ): VoteResult {
        val response =
            feedApiService
                .voteGuestFeed(
                    feedId = feedId,
                    request = VoteRequest(choice = choice.name),
                ).getOrThrow()
        return response.toDomain()
    }
}

/**
 * DTO to Domain Mappers
 */
private fun FeedItemDto.toDomain(): Feed =
    Feed(
        feedId = feedId,
        content = content,
        price = price,
        category = category,
        yesCount = yesCount,
        noCount = noCount,
        totalCount = totalCount,
        feedStatus = feedStatus.toFeedStatus(),
        s3ObjectKey = s3ObjectKey,
        viewUrl = viewUrl,
        imageWidth = imageWidth,
        imageHeight = imageHeight,
        author = author.toDomain(),
        createdAt = createdAt,
        hasVoted = hasVoted,
        myVoteChoice = myVoteChoice?.toVoteChoice(),
    )

private fun AuthorDto.toDomain(): Author =
    Author(
        userId = userId,
        nickname = nickname,
        profileImage = profileImage,
    )

private fun String.toVoteChoice(): VoteChoice? =
    when (this) {
        "YES" -> VoteChoice.YES
        "NO" -> VoteChoice.NO
        else -> null
    }

private fun String.toFeedStatus(): FeedStatus =
    when (this) {
        "OPEN" -> FeedStatus.OPEN
        "CLOSED" -> FeedStatus.CLOSED
        else -> FeedStatus.CLOSED
    }

private fun VoteResponse.toDomain(): VoteResult =
    VoteResult(
        feedId = feedId,
        choice = choice.toVoteChoice() ?: error("Unexpected vote choice from server: $choice"),
        yesCount = yesCount,
        noCount = noCount,
        totalCount = totalCount,
    )
