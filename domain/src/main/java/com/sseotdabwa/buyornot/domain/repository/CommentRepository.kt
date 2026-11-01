package com.sseotdabwa.buyornot.domain.repository

import com.sseotdabwa.buyornot.domain.model.CommentLike
import com.sseotdabwa.buyornot.domain.model.CommentPage
import com.sseotdabwa.buyornot.domain.model.CommentReplyPage
import com.sseotdabwa.buyornot.domain.model.CommentSort
import com.sseotdabwa.buyornot.domain.model.CommentThread
import kotlinx.coroutines.flow.SharedFlow

/**
 * 피드 댓글 저장소. 서버가 거절한 요청은
 * [com.sseotdabwa.buyornot.domain.exception.ApiException]으로 던지며, code는
 * [com.sseotdabwa.buyornot.domain.model.CommentErrorCode], message는 서버 메시지다.
 */
interface CommentRepository {
    /** 댓글·답글이 작성·삭제된 피드 id. 피드 목록의 댓글 수·미리보기를 다시 불러오는 신호로 쓴다. */
    val commentChanges: SharedFlow<Long>

    /**
     * 답글이 작성·삭제·신고되거나 공감이 바뀌거나 원댓글이 삭제·신고된 스레드.
     * 다른 화면에 떠 있는 같은 스레드를 다시 불러오는 신호로 쓴다.
     */
    val threadChanges: SharedFlow<CommentThreadChange>

    suspend fun getComments(
        feedId: Long,
        cursor: Long? = null,
        size: Int = 20,
        sort: CommentSort = CommentSort.REGISTERED,
    ): CommentPage

    /** @return 생성된 댓글 id */
    suspend fun createComment(
        feedId: Long,
        content: String,
    ): Long

    suspend fun deleteComment(
        feedId: Long,
        commentId: Long,
    )

    suspend fun reportComment(
        feedId: Long,
        commentId: Long,
    )

    /** 답글 페이지 상단 고정 원댓글과 헤더 개수. */
    suspend fun getCommentThread(
        feedId: Long,
        commentId: Long,
    ): CommentThread

    /** 등록순 답글 목록 (커서 페이징). */
    suspend fun getReplies(
        feedId: Long,
        commentId: Long,
        cursor: Long? = null,
        size: Int = 20,
    ): CommentReplyPage

    /** @return 생성된 답글 id */
    suspend fun createReply(
        feedId: Long,
        commentId: Long,
        content: String,
    ): Long

    suspend fun deleteReply(
        feedId: Long,
        commentId: Long,
        replyId: Long,
    )

    suspend fun reportReply(
        feedId: Long,
        commentId: Long,
        replyId: Long,
    )

    suspend fun likeComment(
        feedId: Long,
        commentId: Long,
    ): CommentLike

    suspend fun unlikeComment(
        feedId: Long,
        commentId: Long,
    ): CommentLike
}

data class CommentThreadChange(
    val feedId: Long,
    val commentId: Long,
)
