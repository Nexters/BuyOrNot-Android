package com.sseotdabwa.buyornot.core.network.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedListResponse(
    @SerialName("content")
    val content: List<FeedItemDto>,
    @SerialName("nextCursor")
    val nextCursor: Long?,
    @SerialName("hasNext")
    val hasNext: Boolean,
)

@Serializable
data class FeedImageDto(
    @SerialName("s3ObjectKey")
    val s3ObjectKey: String,
    @SerialName("imageUrl")
    val imageUrl: String,
    @SerialName("imageWidth")
    val imageWidth: Int,
    @SerialName("imageHeight")
    val imageHeight: Int,
)

@Serializable
data class FeedItemDto(
    @SerialName("feedId")
    val feedId: Long,
    @SerialName("content")
    val content: String,
    @SerialName("price")
    val price: Int,
    @SerialName("category")
    val category: String,
    @SerialName("yesCount")
    val yesCount: Int,
    @SerialName("noCount")
    val noCount: Int,
    @SerialName("totalCount")
    val totalCount: Int,
    @SerialName("feedStatus")
    val feedStatus: String,
    @SerialName("images")
    val images: List<FeedImageDto>,
    @SerialName("link")
    val link: String? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("author")
    val author: AuthorDto,
    @SerialName("createdAt")
    val createdAt: String,
    @SerialName("hasVoted")
    val hasVoted: Boolean?,
    @SerialName("myVoteChoice")
    val myVoteChoice: String?,
)

@Serializable
data class AuthorDto(
    @SerialName("userId")
    val userId: Long,
    @SerialName("nickname")
    val nickname: String,
    @SerialName("profileImage")
    val profileImage: String?,
)
