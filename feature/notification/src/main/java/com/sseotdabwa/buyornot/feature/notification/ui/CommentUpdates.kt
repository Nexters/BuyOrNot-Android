package com.sseotdabwa.buyornot.feature.notification.ui

import com.sseotdabwa.buyornot.domain.exception.ApiException
import com.sseotdabwa.buyornot.domain.model.Comment
import com.sseotdabwa.buyornot.domain.model.CommentErrorCode
import com.sseotdabwa.buyornot.domain.model.CommentLike
import com.sseotdabwa.buyornot.domain.model.CommentReply
import com.sseotdabwa.buyornot.domain.repository.CommentRepository

/**
 * 삭제·신고·공감 대상. [replyId]가 있으면 [commentId]에 달린 답글이고, 없으면 원댓글이다.
 */
data class CommentTarget(
    val commentId: Long,
    val replyId: Long? = null,
)

internal suspend fun CommentRepository.delete(
    feedId: Long,
    target: CommentTarget,
) {
    if (target.replyId == null) {
        deleteComment(feedId = feedId, commentId = target.commentId)
    } else {
        deleteReply(feedId = feedId, commentId = target.commentId, replyId = target.replyId)
    }
}

internal suspend fun CommentRepository.report(
    feedId: Long,
    target: CommentTarget,
) {
    if (target.replyId == null) {
        reportComment(feedId = feedId, commentId = target.commentId)
    } else {
        reportReply(feedId = feedId, commentId = target.commentId, replyId = target.replyId)
    }
}

/** [isLiked]가 지금 공감한 상태이면 공감을 취소하고, 아니면 공감한다. */
internal suspend fun CommentRepository.toggleLike(
    feedId: Long,
    target: CommentTarget,
    isLiked: Boolean,
): CommentLike {
    val replyId = target.replyId
    return when {
        replyId == null && isLiked -> unlikeComment(feedId = feedId, commentId = target.commentId)
        replyId == null -> likeComment(feedId = feedId, commentId = target.commentId)
        isLiked -> unlikeReply(feedId = feedId, commentId = target.commentId, replyId = replyId)
        else -> likeReply(feedId = feedId, commentId = target.commentId, replyId = replyId)
    }
}

// 한 스레드의 답글은 많지 않아 커서를 끝까지 넘겨 모두 받는다.
internal suspend fun CommentRepository.getAllReplies(
    feedId: Long,
    commentId: Long,
): List<CommentReply> =
    buildList {
        var cursor: Long? = null
        do {
            val page = getReplies(feedId = feedId, commentId = commentId, cursor = cursor)
            addAll(page.replies)
            cursor = page.nextCursor
        } while (page.hasNext && cursor != null)
    }.distinctBy { it.id }

internal fun List<Comment>.updateComment(
    commentId: Long,
    transform: (Comment) -> Comment,
): List<Comment> = map { if (it.id == commentId) transform(it) else it }

internal fun List<CommentReply>.updateReply(
    replyId: Long,
    transform: (CommentReply) -> CommentReply,
): List<CommentReply> = map { if (it.id == replyId) transform(it) else it }

/** [target]이 가리키는 원댓글 또는 답글의 지금 공감 상태. 목록에 없으면 null. */
internal fun Comment.likeOf(target: CommentTarget): CommentLike? {
    val replyId = target.replyId ?: return CommentLike(likeCount = likeCount, isLiked = isLiked)
    return replies.firstOrNull { it.id == replyId }?.let { CommentLike(likeCount = it.likeCount, isLiked = it.isLiked) }
}

internal fun Comment.withLike(
    target: CommentTarget,
    like: CommentLike,
): Comment {
    val replyId = target.replyId ?: return copy(likeCount = like.likeCount, isLiked = like.isLiked)
    return copy(replies = replies.updateReply(replyId) { it.copy(likeCount = like.likeCount, isLiked = like.isLiked) })
}

internal fun CommentLike.toggled(): CommentLike =
    if (isLiked) {
        CommentLike(likeCount = (likeCount - 1).coerceAtLeast(0), isLiked = false)
    } else {
        CommentLike(likeCount = likeCount + 1, isLiked = true)
    }

internal fun Comment.withoutReply(replyId: Long): Comment {
    if (replies.none { it.id == replyId }) return this
    return copy(replies = replies.filterNot { it.id == replyId }, replyCount = (replyCount - 1).coerceAtLeast(0))
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
