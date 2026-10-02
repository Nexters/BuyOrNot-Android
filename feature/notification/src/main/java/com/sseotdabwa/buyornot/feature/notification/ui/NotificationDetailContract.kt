package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.compose.runtime.Immutable
import com.sseotdabwa.buyornot.core.designsystem.icon.IconResource
import com.sseotdabwa.buyornot.domain.model.COMMENT_MAX_LENGTH
import com.sseotdabwa.buyornot.domain.model.Comment
import com.sseotdabwa.buyornot.domain.model.CommentSort
import com.sseotdabwa.buyornot.domain.model.Feed
import com.sseotdabwa.buyornot.domain.model.FeedStatus

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
    val deletingCommentId: Long? = null,
    val reportingCommentId: Long? = null,
    /** 댓글 진입점으로 들어왔을 때 댓글 영역으로 한 번 스크롤한다. */
    val pendingScrollToComments: Boolean = false,
    /** [의견 남기기]로 들어왔을 때 입력창에 한 번 포커스한다. */
    val pendingFocusCommentInput: Boolean = false,
) {
    private val hasJoinedVote: Boolean get() = isOwner || feed?.hasVoted == true

    /** 투표 후에만 댓글을 볼 수 있다. 마감된 투표는 더 이상 투표할 수 없으니 누구나 본다. */
    val canViewComments: Boolean
        get() = feed != null && (hasJoinedVote || feed.feedStatus == FeedStatus.CLOSED)

    /** 진행 중인 투표에 참여한 회원만 쓸 수 있다. 피드 작성자는 자기 글에 투표하지 않으므로 예외. */
    val canWriteComment: Boolean
        get() = feed != null && !isGuest && hasJoinedVote && feed.feedStatus == FeedStatus.OPEN

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
        val commentId: Long,
    ) : NotificationDetailIntent

    data object DismissDeleteCommentDialog : NotificationDetailIntent

    data object OnDeleteCommentConfirmed : NotificationDetailIntent

    data class ShowReportCommentDialog(
        val commentId: Long,
    ) : NotificationDetailIntent

    data object DismissReportCommentDialog : NotificationDetailIntent

    data object OnReportCommentConfirmed : NotificationDetailIntent

    data object OnCommentScrollHandled : NotificationDetailIntent

    data object OnCommentFocusHandled : NotificationDetailIntent
}

sealed interface NotificationDetailSideEffect {
    data class ShowSnackbar(
        val message: String,
        val icon: IconResource? = null,
    ) : NotificationDetailSideEffect

    data object NavigateBack : NotificationDetailSideEffect
}
