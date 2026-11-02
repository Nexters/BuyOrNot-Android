package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.compose.runtime.Immutable
import com.sseotdabwa.buyornot.core.designsystem.components.ImageAspectRatio
import com.sseotdabwa.buyornot.core.designsystem.icon.IconResource
import com.sseotdabwa.buyornot.core.designsystem.image.ImageModel
import com.sseotdabwa.buyornot.domain.model.COMMENT_MAX_LENGTH
import com.sseotdabwa.buyornot.domain.model.Comment
import com.sseotdabwa.buyornot.domain.model.CommentSort
import com.sseotdabwa.buyornot.domain.model.FeedCategory
import com.sseotdabwa.buyornot.domain.model.FeedStatus
import com.sseotdabwa.buyornot.domain.model.VoteChoice

/**
 * @property canBlockAuthor 비회원이 쓴 글은 차단할 유저가 없어 false.
 */
@Immutable
data class NotificationDetailFeed(
    val feedId: Long,
    val title: String,
    val content: String,
    val price: String,
    val category: FeedCategory,
    val yesCount: Int,
    val noCount: Int,
    val totalCount: Int,
    val feedStatus: FeedStatus,
    val productImages: List<ImageModel>,
    val imageAspectRatios: List<ImageAspectRatio>,
    val authorUserId: Long,
    val authorNickname: String,
    val authorProfileImage: ImageModel?,
    val canBlockAuthor: Boolean,
    val createdAt: String,
    val hasVoted: Boolean,
    val myVoteChoice: VoteChoice?,
    val productLink: String? = null,
    val commentCount: Int = 0,
)

@Immutable
data class NotificationDetailUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val feed: NotificationDetailFeed? = null,
    val voterProfileImage: ImageModel? = null,
    val isOwner: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showBlockDialog: Boolean = false,
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

    /** 앱은 회원만 쓰므로 투표 여부·마감과 상관없이 피드를 불러왔으면 댓글·답글을 쓰고 공감할 수 있다. */
    val canWriteComment: Boolean
        get() = feed != null

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
