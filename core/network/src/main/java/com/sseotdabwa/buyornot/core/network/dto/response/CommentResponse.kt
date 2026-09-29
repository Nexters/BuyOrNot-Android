package com.sseotdabwa.buyornot.core.network.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentCreateResponse(
    @SerialName("id")
    val id: Long,
)

@Serializable
data class CommentListResponse(
    @SerialName("content")
    val content: List<CommentDto>,
    @SerialName("nextCursor")
    val nextCursor: Long? = null,
    @SerialName("hasNext")
    val hasNext: Boolean,
)

@Serializable
data class CommentDto(
    @SerialName("id")
    val id: Long,
    @SerialName("authorType")
    val authorType: String, // "MEMBER" or "GUEST"
    @SerialName("nickname")
    val nickname: String,
    @SerialName("profileImage")
    val profileImage: String? = null,
    @SerialName("content")
    val content: String,
    @SerialName("isMine")
    val isMine: Boolean = false,
    @SerialName("isAuthor")
    val isAuthor: Boolean = false,
    @SerialName("voteChoice")
    val voteChoice: String? = null, // "YES", "NO" or null
    @SerialName("createdAt")
    val createdAt: String,
)

/** 피드 응답의 가장 최근 댓글 미리보기. 댓글이 없으면 필드 자체가 없다. */
@Serializable
data class LatestCommentDto(
    @SerialName("authorType")
    val authorType: String,
    @SerialName("nickname")
    val nickname: String,
    @SerialName("profileImage")
    val profileImage: String? = null,
    @SerialName("content")
    val content: String,
    @SerialName("isAuthor")
    val isAuthor: Boolean = false,
    @SerialName("voteChoice")
    val voteChoice: String? = null,
)
