package com.sseotdabwa.buyornot.core.data.repository

import com.sseotdabwa.buyornot.core.network.api.CommentApiService
import com.sseotdabwa.buyornot.core.network.dto.request.CommentRequest
import com.sseotdabwa.buyornot.core.network.dto.response.CommentDto
import com.sseotdabwa.buyornot.core.network.dto.response.CommentLikeResponse
import com.sseotdabwa.buyornot.core.network.dto.response.CommentListResponse
import com.sseotdabwa.buyornot.core.network.dto.response.CommentReplyDto
import com.sseotdabwa.buyornot.core.network.dto.response.CommentReplyListResponse
import com.sseotdabwa.buyornot.core.network.dto.response.apiErrorOrNull
import com.sseotdabwa.buyornot.core.network.dto.response.getOrThrow
import com.sseotdabwa.buyornot.domain.exception.ApiException
import com.sseotdabwa.buyornot.domain.model.Comment
import com.sseotdabwa.buyornot.domain.model.CommentLike
import com.sseotdabwa.buyornot.domain.model.CommentPage
import com.sseotdabwa.buyornot.domain.model.CommentReply
import com.sseotdabwa.buyornot.domain.model.CommentReplyPage
import com.sseotdabwa.buyornot.domain.model.CommentSort
import com.sseotdabwa.buyornot.domain.model.CommentThread
import com.sseotdabwa.buyornot.domain.repository.CommentRepository
import com.sseotdabwa.buyornot.domain.repository.CommentThreadChange
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton
import com.sseotdabwa.buyornot.core.network.dto.response.CommentThreadResponse as CommentThreadResponseDto

// commentChanges를 구독자와 공유해야 하므로 단일 인스턴스여야 한다.
@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val commentApiService: CommentApiService,
) : CommentRepository {
    // 구독자가 없거나 느려도 emit이 멈추지 않도록 버퍼를 두고, 넘치면 오래된 신호를 버린다.
    private val _commentChanges =
        MutableSharedFlow<Long>(extraBufferCapacity = 16, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val commentChanges = _commentChanges.asSharedFlow()

    private val _threadChanges =
        MutableSharedFlow<CommentThreadChange>(extraBufferCapacity = 16, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val threadChanges = _threadChanges.asSharedFlow()

    override suspend fun getComments(
        feedId: Long,
        cursor: Long?,
        size: Int,
        sort: CommentSort,
    ): CommentPage =
        withApiException {
            commentApiService
                .getComments(feedId = feedId, cursor = cursor, size = size, sort = sort.name)
                .getOrThrow()
                .toDomain()
        }

    override suspend fun createComment(
        feedId: Long,
        content: String,
    ): Long =
        withApiException {
            commentApiService
                .createComment(feedId, CommentRequest(content = content))
                .getOrThrow()
                .id
        }.also { _commentChanges.tryEmit(feedId) }

    override suspend fun deleteComment(
        feedId: Long,
        commentId: Long,
    ) {
        withApiException { commentApiService.deleteComment(feedId, commentId).getOrThrow() }
        _commentChanges.tryEmit(feedId)
        // 답글 페이지를 열어둔 다른 화면이 이 원댓글의 삭제를 반영하도록 함께 알린다.
        _threadChanges.tryEmit(CommentThreadChange(feedId, commentId))
    }

    override suspend fun reportComment(
        feedId: Long,
        commentId: Long,
    ) {
        // 신고된 댓글은 목록·개수에서 빠지므로 피드 미리보기도 다시 불러와야 한다.
        withApiException { commentApiService.reportComment(feedId, commentId).getOrThrow() }
        _commentChanges.tryEmit(feedId)
        _threadChanges.tryEmit(CommentThreadChange(feedId, commentId))
    }

    override suspend fun getCommentThread(
        feedId: Long,
        commentId: Long,
    ): CommentThread =
        withApiException {
            commentApiService
                .getCommentThread(feedId, commentId)
                .getOrThrow()
                .toDomain()
        }

    override suspend fun getReplies(
        feedId: Long,
        commentId: Long,
        cursor: Long?,
        size: Int,
    ): CommentReplyPage =
        withApiException {
            commentApiService
                .getReplies(feedId = feedId, commentId = commentId, cursor = cursor, size = size)
                .getOrThrow()
                .toDomain()
        }

    override suspend fun createReply(
        feedId: Long,
        commentId: Long,
        content: String,
    ): Long =
        withApiException {
            commentApiService
                .createReply(feedId, commentId, CommentRequest(content = content))
                .getOrThrow()
                .id
        }.also {
            // 피드 commentCount는 원댓글+답글 수이므로 답글 작성도 commentChanges로 알린다.
            _commentChanges.tryEmit(feedId)
            _threadChanges.tryEmit(CommentThreadChange(feedId, commentId))
        }

    override suspend fun deleteReply(
        feedId: Long,
        commentId: Long,
        replyId: Long,
    ) {
        withApiException { commentApiService.deleteComment(feedId, replyId).getOrThrow() }
        _commentChanges.tryEmit(feedId)
        _threadChanges.tryEmit(CommentThreadChange(feedId, commentId))
    }

    override suspend fun reportReply(
        feedId: Long,
        commentId: Long,
        replyId: Long,
    ) {
        withApiException { commentApiService.reportComment(feedId, replyId).getOrThrow() }
        _commentChanges.tryEmit(feedId)
        _threadChanges.tryEmit(CommentThreadChange(feedId, commentId))
    }

    override suspend fun likeComment(
        feedId: Long,
        commentId: Long,
    ): CommentLike =
        withApiException {
            commentApiService
                .likeComment(feedId, commentId)
                .getOrThrow()
                .toDomain()
        }.also { _threadChanges.tryEmit(CommentThreadChange(feedId, commentId)) }

    override suspend fun unlikeComment(
        feedId: Long,
        commentId: Long,
    ): CommentLike =
        withApiException {
            commentApiService
                .unlikeComment(feedId, commentId)
                .getOrThrow()
                .toDomain()
        }.also { _threadChanges.tryEmit(CommentThreadChange(feedId, commentId)) }

    // 금칙어·빈도 제한은 서버 메시지를 그대로 노출하므로 코드와 함께 메시지도 옮긴다.
    private inline fun <T> withApiException(block: () -> T): T =
        try {
            block()
        } catch (e: HttpException) {
            val error = e.apiErrorOrNull()
            throw ApiException(code = error?.code, message = error?.message ?: e.message(), cause = e)
        }
}

private fun CommentListResponse.toDomain(): CommentPage =
    CommentPage(
        comments = content.map { it.toDomain() },
        nextCursor = nextCursor,
        hasNext = hasNext,
    )

private fun CommentDto.toDomain(): Comment =
    Comment(
        id = id,
        authorType = authorType.toCommentAuthorType(),
        nickname = nickname.orEmpty(),
        profileImage = profileImage,
        content = content.orEmpty(),
        isMine = isMine,
        isAuthor = isAuthor,
        voteChoice = voteChoice?.toVoteChoice(),
        createdAt = createdAt,
        isDeleted = isDeleted,
        likeCount = likeCount.toIntSaturated(),
        isLiked = isLiked,
        replyCount = replyCount.toIntSaturated(),
        replies = replies.map { it.toDomain() },
    )

private fun CommentReplyDto.toDomain(): CommentReply =
    CommentReply(
        id = id,
        parentId = parentId,
        authorType = authorType.toCommentAuthorType(),
        nickname = nickname.orEmpty(),
        profileImage = profileImage,
        content = content.orEmpty(),
        isMine = isMine,
        isAuthor = isAuthor,
        createdAt = createdAt,
    )

private fun CommentThreadResponseDto.toDomain(): CommentThread =
    CommentThread(
        comment = comment.toDomain(),
        totalCount = totalCount.toIntSaturated(),
    )

private fun CommentReplyListResponse.toDomain(): CommentReplyPage =
    CommentReplyPage(
        replies = content.map { it.toDomain() },
        nextCursor = nextCursor,
        hasNext = hasNext,
    )

private fun CommentLikeResponse.toDomain(): CommentLike =
    CommentLike(
        likeCount = likeCount.toIntSaturated(),
        isLiked = isLiked,
    )

// 서버 개수는 Long이지만 화면은 Int로 다루므로 넘치면 Int 상한에서 멈춘다.
private fun Long.toIntSaturated(): Int = coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
