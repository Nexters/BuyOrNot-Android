package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.compose.runtime.Immutable
import com.sseotdabwa.buyornot.core.designsystem.icon.IconResource
import com.sseotdabwa.buyornot.domain.model.COMMENT_MAX_LENGTH
import com.sseotdabwa.buyornot.domain.model.Comment
import com.sseotdabwa.buyornot.domain.model.CommentSort
import com.sseotdabwa.buyornot.domain.model.Feed

@Immutable
data class NotificationDetailUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val feed: Feed? = null,
    val voterProfileImageUrl: String = "",
    val isOwner: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showBlockDialog: Boolean = false,
    val isGuest: Boolean = false,
    val isVoting: Boolean = false,
    val comments: List<Comment> = emptyList(),
    val commentSort: CommentSort = CommentSort.REGISTERED,
    val isCommentsLoading: Boolean = false,
    val isNextCommentsLoading: Boolean = false,
    val hasNextComments: Boolean = false,
    val nextCommentCursor: Long? = null,
    val commentInput: String = "",
    val isSubmittingComment: Boolean = false,
    /** 금칙어로 거절된 뒤 내용을 고치기 전까지 등록을 막는다. */
    val isCommentSubmitBlocked: Boolean = false,
    val deletingComment: CommentTarget? = null,
    val reportingComment: CommentTarget? = null,
    /** 댓글 진입점으로 들어왔을 때 댓글 영역으로 한 번 스크롤한다. */
    val pendingScrollToComments: Boolean = false,
    /** [의견 남기기]로 들어왔을 때 입력창에 한 번 포커스한다. */
    val pendingFocusCommentInput: Boolean = false,
) {
    /** 투표 여부·마감과 상관없이 누구나 댓글을 본다. */
    val canViewComments: Boolean
        get() = feed != null

    /** 투표 여부·마감과 상관없이 회원이면 댓글·답글을 쓰고 공감할 수 있다. */
    val canWriteComment: Boolean
        get() = feed != null && !isGuest

    val canSubmitComment: Boolean
        get() =
            canWriteComment &&
                commentInput.isNotBlank() &&
                commentInput.trim().length <= COMMENT_MAX_LENGTH &&
                !isSubmittingComment &&
                !isCommentSubmitBlocked
}

sealed interface NotificationDetailIntent {
    data object OnRefresh : NotificationDetailIntent

    data object ShowDeleteDialog : NotificationDetailIntent

    data object DismissDeleteDialog : NotificationDetailIntent

    data object OnDeleteConfirmed : NotificationDetailIntent

    data object OnReportClicked : NotificationDetailIntent

    data object ShowBlockDialog : NotificationDetailIntent

    data object DismissBlockDialog : NotificationDetailIntent

    data object OnBlockConfirmed : NotificationDetailIntent

    data object OnShareClicked : NotificationDetailIntent

    data class OnVoteClicked(
        val optionIndex: Int,
    ) : NotificationDetailIntent

    data class OnCommentInputChanged(
        val text: String,
    ) : NotificationDetailIntent

    data object OnCommentSubmit : NotificationDetailIntent

    data class OnCommentSortSelected(
        val sort: CommentSort,
    ) : NotificationDetailIntent

    data object LoadNextComments : NotificationDetailIntent

    data class ShowDeleteCommentDialog(
        val target: CommentTarget,
    ) : NotificationDetailIntent

    data object DismissDeleteCommentDialog : NotificationDetailIntent

    data object OnDeleteCommentConfirmed : NotificationDetailIntent

    data class ShowReportCommentDialog(
        val target: CommentTarget,
    ) : NotificationDetailIntent

    data object DismissReportCommentDialog : NotificationDetailIntent

    data object OnReportCommentConfirmed : NotificationDetailIntent

    /** 원댓글의 [답글쓰기]·답글 수 탭 */
    data class OnReplyClicked(
        val commentId: Long,
    ) : NotificationDetailIntent

    data class OnLikeClicked(
        val commentId: Long,
    ) : NotificationDetailIntent

    data object OnCommentScrollHandled : NotificationDetailIntent

    data object OnCommentFocusHandled : NotificationDetailIntent
}

sealed interface NotificationDetailSideEffect {
    data class ShowSnackbar(
        val message: String,
        val icon: IconResource? = null,
    ) : NotificationDetailSideEffect

    data object NavigateBack : NotificationDetailSideEffect

    data class NavigateToReplies(
        val feedId: Long,
        val commentId: Long,
    ) : NotificationDetailSideEffect
}
