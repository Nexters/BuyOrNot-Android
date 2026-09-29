package com.sseotdabwa.buyornot.domain.repository

import com.sseotdabwa.buyornot.domain.model.CommentPage
import com.sseotdabwa.buyornot.domain.model.CommentSort
import kotlinx.coroutines.flow.SharedFlow

/**
 * 피드 댓글 저장소. 서버가 거절한 요청은
 * [com.sseotdabwa.buyornot.domain.exception.ApiException]으로 던지며, code는
 * [com.sseotdabwa.buyornot.domain.model.CommentErrorCode], message는 서버 메시지다.
 */
interface CommentRepository {
    /** 댓글이 작성·삭제된 피드 id. 피드 목록의 댓글 수·미리보기를 다시 불러오는 신호로 쓴다. */
    val commentChanges: SharedFlow<Long>

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
}
