package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.compose.runtime.Immutable
import com.sseotdabwa.buyornot.domain.model.COMMENT_MAX_LENGTH
import com.sseotdabwa.buyornot.domain.model.Comment
import com.sseotdabwa.buyornot.domain.model.CommentReply

@Immutable
data class CommentRepliesUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val comment: Comment? = null,
    val replies: List<CommentReply> = emptyList(),
    /** 헤더 "댓글 (n)"의 n. */
    val totalCount: Int = 0,
    val voterProfileImageUrl: String = "",
    val replyInput: String = "",
    val isSubmittingReply: Boolean = false,
    /** 금칙어로 거절된 뒤 내용을 고치기 전까지 등록을 막는다. */
    val isReplySubmitBlocked: Boolean = false,
    val deletingComment: CommentTarget? = null,
    val reportingComment: CommentTarget? = null,
    /** 답글을 등록한 뒤 방금 쓴 답글이 보이도록 한 번 끝까지 스크롤한다. */
    val pendingScrollToLatest: Boolean = false,
) {
    /** 지워진 원댓글에는 서버가 답글을 받지 않는다(COMMENT_014). */
    val canWriteReply: Boolean
        get() = comment != null && !comment.isDeleted

    val canSubmitReply: Boolean
        get() =
            canWriteReply &&
                replyInput.isNotBlank() &&
                replyInput.trim().length <= COMMENT_MAX_LENGTH &&
                !isSubmittingReply &&
                !isReplySubmitBlocked
}

sealed interface CommentRepliesIntent {
    data object OnRefresh : CommentRepliesIntent

    data class OnReplyInputChanged(
        val text: String,
    ) : CommentRepliesIntent

    data object OnReplySubmit : CommentRepliesIntent

    data object OnLikeClicked : CommentRepliesIntent

    data class ShowDeleteDialog(
        val target: CommentTarget,
    ) : CommentRepliesIntent

    data object DismissDeleteDialog : CommentRepliesIntent

    data object OnDeleteConfirmed : CommentRepliesIntent

    data class ShowReportDialog(
        val target: CommentTarget,
    ) : CommentRepliesIntent

    data object DismissReportDialog : CommentRepliesIntent

    data object OnReportConfirmed : CommentRepliesIntent

    data object OnScrollToLatestHandled : CommentRepliesIntent
}

sealed interface CommentRepliesSideEffect {
    data class ShowSnackbar(
        val message: String,
    ) : CommentRepliesSideEffect

    /** @property message 돌아간 화면에서 띄울 안내. 이 화면은 바로 닫히므로 앱 전역 스낵바로 보여준다. */
    data class NavigateBack(
        val message: String? = null,
    ) : CommentRepliesSideEffect
}
