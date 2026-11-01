package com.sseotdabwa.buyornot.feature.notification.ui

import com.sseotdabwa.buyornot.domain.exception.ApiException
import com.sseotdabwa.buyornot.domain.model.Comment
import com.sseotdabwa.buyornot.domain.model.CommentErrorCode
import com.sseotdabwa.buyornot.domain.model.CommentLike

/**
 * 삭제·신고 대상. [replyId]가 있으면 [commentId]에 달린 답글이고, 없으면 원댓글이다.
 */
data class CommentTarget(
    val commentId: Long,
    val replyId: Long? = null,
)

internal fun List<Comment>.updateComment(
    commentId: Long,
    transform: (Comment) -> Comment,
): List<Comment> = map { if (it.id == commentId) transform(it) else it }

internal fun Comment.withLike(like: CommentLike): Comment = copy(likeCount = like.likeCount, isLiked = like.isLiked)

internal fun Comment.toggledLike(): Comment =
    if (isLiked) {
        copy(isLiked = false, likeCount = (likeCount - 1).coerceAtLeast(0))
    } else {
        copy(isLiked = true, likeCount = likeCount + 1)
    }

// 답글이 남은 원댓글은 서버가 "삭제된 댓글"로 남겨 두므로 목록에서도 자리를 지킨다.
internal fun Comment.afterDeleted(): Comment? =
    if (replyCount > 0) {
        copy(
            isDeleted = true,
            nickname = "",
            profileImage = null,
            content = "",
            isMine = false,
            isAuthor = false,
            voteChoice = null,
            likeCount = 0,
            isLiked = false,
        )
    } else {
        null
    }

// 삭제된 원댓글은 마지막 답글이 사라지면 서버에서도 정리되므로 함께 뺀다.
internal fun Comment.withoutReply(replyId: Long): Comment? {
    if (replies.none { it.id == replyId }) return this
    val remaining = replies.filterNot { it.id == replyId }
    val remainingCount = (replyCount - 1).coerceAtLeast(0)
    if (isDeleted && remainingCount == 0) return null
    return copy(replies = remaining, replyCount = remainingCount)
}

// 같은 내용으로 다시 보내도 또 거절되는 에러라 내용을 고칠 때까지 등록 버튼을 막는다.
private val SUBMIT_BLOCKING_ERROR_CODES =
    setOf(
        CommentErrorCode.PROFANITY,
        CommentErrorCode.RATE_LIMITED,
        CommentErrorCode.PROFANITY_BLOCKED,
    )

internal fun ApiException?.blocksResubmit(): Boolean = this?.code in SUBMIT_BLOCKING_ERROR_CODES

// 금칙어·빈도 제한은 서버 메시지를 그대로 보여준다 (API 명세).
internal fun ApiException?.createErrorMessage(defaultMessage: String): String =
    when (this?.code) {
        CommentErrorCode.PROFANITY,
        CommentErrorCode.RATE_LIMITED,
        CommentErrorCode.PROFANITY_BLOCKED,
        -> message ?: defaultMessage
        CommentErrorCode.INVALID_PARENT -> "답글을 남길 수 없는 댓글이에요."
        else -> defaultMessage
    }

internal fun ApiException?.commentActionErrorMessage(defaultMessage: String): String =
    when (this?.code) {
        CommentErrorCode.NOT_FOUND -> "이미 삭제된 댓글이에요."
        CommentErrorCode.SELF_REPORT -> "내가 쓴 댓글은 신고할 수 없어요."
        CommentErrorCode.ALREADY_REPORTED -> "이미 신고한 댓글이에요."
        else -> defaultMessage
    }

internal fun ApiException?.likeErrorMessage(): String =
    when (this?.code) {
        CommentErrorCode.NOT_FOUND, CommentErrorCode.LIKE_UNAVAILABLE -> "공감할 수 없는 댓글이에요."
        else -> "공감에 실패했어요. 다시 시도해 주세요."
    }
