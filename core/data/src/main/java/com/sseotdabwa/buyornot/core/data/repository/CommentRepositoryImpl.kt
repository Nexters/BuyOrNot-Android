package com.sseotdabwa.buyornot.core.data.repository

import com.sseotdabwa.buyornot.core.network.api.CommentApiService
import com.sseotdabwa.buyornot.core.network.dto.request.CommentRequest
import com.sseotdabwa.buyornot.core.network.dto.response.CommentDto
import com.sseotdabwa.buyornot.core.network.dto.response.CommentListResponse
import com.sseotdabwa.buyornot.core.network.dto.response.apiErrorOrNull
import com.sseotdabwa.buyornot.core.network.dto.response.getOrThrow
import com.sseotdabwa.buyornot.domain.exception.ApiException
import com.sseotdabwa.buyornot.domain.model.Comment
import com.sseotdabwa.buyornot.domain.model.CommentPage
import com.sseotdabwa.buyornot.domain.model.CommentSort
import com.sseotdabwa.buyornot.domain.repository.CommentRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

// commentChanges를 구독자와 공유해야 하므로 단일 인스턴스여야 한다.
@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val commentApiService: CommentApiService,
) : CommentRepository {
    // 구독자가 없거나 느려도 emit이 멈추지 않도록 버퍼를 두고, 넘치면 오래된 신호를 버린다.
    private val _commentChanges =
        MutableSharedFlow<Long>(extraBufferCapacity = 16, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val commentChanges = _commentChanges.asSharedFlow()

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
    }

    override suspend fun reportComment(
        feedId: Long,
        commentId: Long,
    ) {
        // 신고된 댓글은 목록·개수에서 빠지므로 피드 미리보기도 다시 불러와야 한다.
        withApiException { commentApiService.reportComment(feedId, commentId).getOrThrow() }
        _commentChanges.tryEmit(feedId)
    }

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
        nickname = nickname,
        profileImage = profileImage,
        content = content,
        isMine = isMine,
        isAuthor = isAuthor,
        voteChoice = voteChoice?.toVoteChoice(),
        createdAt = createdAt,
    )
