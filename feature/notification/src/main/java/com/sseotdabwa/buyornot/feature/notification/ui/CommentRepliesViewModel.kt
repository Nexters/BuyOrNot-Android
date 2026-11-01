package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.domain.exception.ApiException
import com.sseotdabwa.buyornot.domain.model.CommentErrorCode
import com.sseotdabwa.buyornot.domain.model.CommentReply
import com.sseotdabwa.buyornot.domain.model.UserType
import com.sseotdabwa.buyornot.domain.repository.CommentRepository
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CommentRepliesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val commentRepository: CommentRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : BaseViewModel<CommentRepliesUiState, CommentRepliesIntent, CommentRepliesSideEffect>(CommentRepliesUiState()) {
    private val feedId: Long = checkNotNull(savedStateHandle[KEY_FEED_ID])
    private val commentId: Long = checkNotNull(savedStateHandle[KEY_COMMENT_ID])

    private var isLikePending = false

    init {
        observeUserPreferences()
        loadThread()
    }

    private fun observeUserPreferences() {
        viewModelScope.launch {
            userPreferencesRepository.userPreferences.collect { preferences ->
                updateState {
                    it.copy(
                        voterProfileImageUrl = preferences.profileImageUrl,
                        isGuest = preferences.userType == UserType.GUEST,
                    )
                }
            }
        }
    }

    override fun handleIntent(intent: CommentRepliesIntent) {
        when (intent) {
            CommentRepliesIntent.OnRefresh -> loadThread()
            is CommentRepliesIntent.OnReplyInputChanged ->
                // 금칙어·작성 빈도 제한으로 막힌 등록은 내용을 고치면 다시 열린다.
                updateState {
                    it.copy(
                        replyInput = intent.text,
                        isReplySubmitBlocked = it.isReplySubmitBlocked && it.replyInput == intent.text,
                    )
                }
            CommentRepliesIntent.OnReplySubmit -> handleReplySubmit()
            CommentRepliesIntent.OnLikeClicked -> handleLike()
            is CommentRepliesIntent.ShowDeleteDialog -> updateState { it.copy(deletingComment = intent.target) }
            CommentRepliesIntent.DismissDeleteDialog -> updateState { it.copy(deletingComment = null) }
            CommentRepliesIntent.OnDeleteConfirmed -> handleDelete()
            is CommentRepliesIntent.ShowReportDialog -> updateState { it.copy(reportingComment = intent.target) }
            CommentRepliesIntent.DismissReportDialog -> updateState { it.copy(reportingComment = null) }
            CommentRepliesIntent.OnReportConfirmed -> handleReport()
            CommentRepliesIntent.OnScrollToLatestHandled -> updateState { it.copy(pendingScrollToLatest = false) }
        }
    }

    private fun loadThread(scrollToLatest: Boolean = false) {
        viewModelScope.launch {
            if (currentState.comment == null) updateState { it.copy(isLoading = true, isError = false) }
            runCatchingCancellable {
                coroutineScope {
                    val thread = async { commentRepository.getCommentThread(feedId = feedId, commentId = commentId) }
                    val replies = async { loadAllReplies() }
                    thread.await() to replies.await()
                }
            }.onSuccess { (thread, replies) ->
                updateState {
                    it.copy(
                        isLoading = false,
                        isError = false,
                        comment = thread.comment,
                        replies = replies,
                        totalCount = thread.totalCount,
                        pendingScrollToLatest = it.pendingScrollToLatest || scrollToLatest,
                    )
                }
            }.onFailure { e ->
                Timber.e(e, "Failed to load comment thread: $commentId")
                if ((e as? ApiException)?.code == CommentErrorCode.NOT_FOUND) {
                    sendSideEffect(CommentRepliesSideEffect.ShowSnackbar(message = "이미 삭제된 댓글이에요."))
                    sendSideEffect(CommentRepliesSideEffect.NavigateBack)
                    return@onFailure
                }
                updateState { it.copy(isLoading = false, isError = it.comment == null) }
            }
        }
    }

    // 한 스레드의 답글은 많지 않아 한 화면에 모두 그린다.
    private suspend fun loadAllReplies(): List<CommentReply> =
        buildList {
            var cursor: Long? = null
            do {
                val page = commentRepository.getReplies(feedId = feedId, commentId = commentId, cursor = cursor)
                addAll(page.replies)
                cursor = page.nextCursor
            } while (page.hasNext && cursor != null)
        }.distinctBy { it.id }

    private fun handleReplySubmit() {
        if (!currentState.canSubmitReply) return
        val content = currentState.replyInput.trim()
        updateState { it.copy(isSubmittingReply = true) }
        viewModelScope.launch {
            runCatchingCancellable {
                commentRepository.createReply(feedId = feedId, commentId = commentId, content = content)
            }.onSuccess {
                updateState { it.copy(isSubmittingReply = false, replyInput = "") }
                sendSideEffect(CommentRepliesSideEffect.ShowSnackbar(message = "의견을 남겼어요!"))
                loadThread(scrollToLatest = true)
            }.onFailure { e ->
                Timber.e(e, "Failed to create reply: $commentId")
                val apiError = e as? ApiException
                updateState { it.copy(isSubmittingReply = false, isReplySubmitBlocked = apiError.blocksResubmit()) }
                sendSideEffect(
                    CommentRepliesSideEffect.ShowSnackbar(message = apiError.createErrorMessage(defaultMessage = "답글 등록에 실패했어요.")),
                )
            }
        }
    }

    private fun handleLike() {
        val comment = currentState.comment ?: return
        if (currentState.isGuest || comment.isDeleted || isLikePending) return
        isLikePending = true
        updateState { it.copy(comment = comment.toggledLike()) }
        viewModelScope.launch {
            runCatchingCancellable {
                if (comment.isLiked) {
                    commentRepository.unlikeComment(feedId = feedId, commentId = commentId)
                } else {
                    commentRepository.likeComment(feedId = feedId, commentId = commentId)
                }
            }.onSuccess { like ->
                updateState { it.copy(comment = it.comment?.withLike(like)) }
            }.onFailure { e ->
                Timber.e(e, "Failed to toggle like: $commentId")
                updateState { it.copy(comment = it.comment?.copy(likeCount = comment.likeCount, isLiked = comment.isLiked)) }
                sendSideEffect(CommentRepliesSideEffect.ShowSnackbar(message = (e as? ApiException).likeErrorMessage()))
            }
            isLikePending = false
        }
    }

    private fun handleDelete() {
        val target = currentState.deletingComment ?: return
        updateState { it.copy(deletingComment = null) }
        viewModelScope.launch {
            runCatchingCancellable {
                if (target.replyId == null) {
                    commentRepository.deleteComment(feedId = feedId, commentId = target.commentId)
                } else {
                    commentRepository.deleteReply(feedId = feedId, commentId = target.commentId, replyId = target.replyId)
                }
            }.onSuccess {
                sendSideEffect(CommentRepliesSideEffect.ShowSnackbar(message = "삭제가 완료되었어요."))
                removeTarget(target, isDeleted = true)
            }.onFailure { e ->
                Timber.e(e, "Failed to delete comment: $target")
                if ((e as? ApiException)?.code == CommentErrorCode.NOT_FOUND) removeTarget(target, isDeleted = false)
                sendSideEffect(
                    CommentRepliesSideEffect.ShowSnackbar(
                        message = (e as? ApiException).commentActionErrorMessage(defaultMessage = "삭제에 실패했어요."),
                    ),
                )
            }
        }
    }

    private fun handleReport() {
        val target = currentState.reportingComment ?: return
        updateState { it.copy(reportingComment = null) }
        viewModelScope.launch {
            runCatchingCancellable {
                if (target.replyId == null) {
                    commentRepository.reportComment(feedId = feedId, commentId = target.commentId)
                } else {
                    commentRepository.reportReply(feedId = feedId, commentId = target.commentId, replyId = target.replyId)
                }
            }.onSuccess {
                sendSideEffect(CommentRepliesSideEffect.ShowSnackbar(message = "댓글 신고가 접수되었어요."))
                removeTarget(target, isDeleted = false)
            }.onFailure { e ->
                Timber.e(e, "Failed to report comment: $target")
                if ((e as? ApiException)?.code == CommentErrorCode.NOT_FOUND) removeTarget(target, isDeleted = false)
                sendSideEffect(
                    CommentRepliesSideEffect.ShowSnackbar(
                        message = (e as? ApiException).commentActionErrorMessage(defaultMessage = "신고에 실패했어요."),
                    ),
                )
            }
        }
    }

    /**
     * 원댓글이 목록에서 완전히 사라지면 이 페이지는 더 보여줄 게 없으므로 상세로 돌아간다.
     *
     * @param isDeleted 작성자가 지웠으면 답글이 남은 원댓글을 "삭제된 댓글"로 남긴다. 신고·이미 없음이면 바로 뺀다.
     */
    private fun removeTarget(
        target: CommentTarget,
        isDeleted: Boolean,
    ) {
        val comment = currentState.comment ?: return
        if (target.replyId != null) {
            val remaining = currentState.replies.filterNot { it.id == target.replyId }
            if (remaining.size == currentState.replies.size) return
            // 삭제된 원댓글은 마지막 답글이 사라지면 서버에서도 정리된다.
            if (comment.isDeleted && remaining.isEmpty()) {
                sendSideEffect(CommentRepliesSideEffect.NavigateBack)
                return
            }
            updateState {
                it.copy(
                    comment = comment.copy(replyCount = remaining.size),
                    replies = remaining,
                    totalCount = (it.totalCount - 1).coerceAtLeast(0),
                )
            }
            return
        }
        val updated = if (isDeleted) comment.copy(replyCount = currentState.replies.size).afterDeleted() else null
        if (updated == null) {
            sendSideEffect(CommentRepliesSideEffect.NavigateBack)
            return
        }
        updateState { it.copy(comment = updated, totalCount = (it.totalCount - 1).coerceAtLeast(0)) }
    }

    companion object {
        // CommentRepliesRoute의 인자 이름과 같아야 한다.
        const val KEY_FEED_ID = "feedId"
        const val KEY_COMMENT_ID = "commentId"
    }
}
