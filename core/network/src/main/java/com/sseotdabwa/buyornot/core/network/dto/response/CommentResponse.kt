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
    val nickname: String? = null,
    @SerialName("profileImage")
    val profileImage: String? = null,
    @SerialName("content")
    val content: String? = null,
    @SerialName("isMine")
    val isMine: Boolean = false,
    @SerialName("isAuthor")
    val isAuthor: Boolean = false,
    @SerialName("voteChoice")
    val voteChoice: String? = null, // "YES", "NO" or null
    @SerialName("createdAt")
    val createdAt: String,
    @SerialName("isDeleted")
    val isDeleted: Boolean = false,
    @SerialName("likeCount")
    val likeCount: Long = 0,
    @SerialName("isLiked")
    val isLiked: Boolean = false,
    @SerialName("replyCount")
    val replyCount: Long = 0,
    @SerialName("replies")
    val replies: List<CommentReplyDto> = emptyList(),
)

@Serializable
data class CommentReplyDto(
    @SerialName("id")
    val id: Long,
    @SerialName("parentId")
    val parentId: Long,
    @SerialName("authorType")
    val authorType: String,
    @SerialName("nickname")
    val nickname: String? = null,
    @SerialName("profileImage")
    val profileImage: String? = null,
    @SerialName("content")
    val content: String? = null,
    @SerialName("isMine")
    val isMine: Boolean = false,
    @SerialName("isAuthor")
    val isAuthor: Boolean = false,
    @SerialName("createdAt")
    val createdAt: String,
)

@Serializable
data class CommentReplyListResponse(
    @SerialName("content")
    val content: List<CommentReplyDto>,
    @SerialName("nextCursor")
    val nextCursor: Long? = null,
    @SerialName("hasNext")
    val hasNext: Boolean,
)

@Serializable
data class CommentThreadResponse(
    @SerialName("comment")
    val comment: CommentDto,
    @SerialName("totalCount")
    val totalCount: Long,
)

@Serializable
data class CommentLikeResponse(
    @SerialName("likeCount")
    val likeCount: Long,
    @SerialName("isLiked")
    val isLiked: Boolean,
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
