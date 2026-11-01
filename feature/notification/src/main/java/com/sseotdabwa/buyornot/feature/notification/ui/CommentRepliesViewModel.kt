package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.domain.exception.ApiException
import com.sseotdabwa.buyornot.domain.model.CommentErrorCode
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
                updateState { it.copy(voterProfileImageUrl = preferences.profileImageUrl) }
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
                    val replies = async { commentRepository.getAllReplies(feedId = feedId, commentId = commentId) }
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
                    sendSideEffect(CommentRepliesSideEffect.NavigateBack(message = "이미 삭제된 댓글이에요."))
                    return@onFailure
                }
                updateState { it.copy(isLoading = false, isError = it.comment == null) }
            }
        }
    }

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
        if (comment.isDeleted || isLikePending) return
        isLikePending = true
        updateState { it.copy(comment = comment.toggledLike()) }
        viewModelScope.launch {
            runCatchingCancellable {
                commentRepository.toggleLike(feedId = feedId, comment = comment)
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
                commentRepository.delete(feedId = feedId, target = target)
            }.onSuccess {
                removeTarget(target, isDeleted = true, message = "삭제가 완료되었어요.")
            }.onFailure { e ->
                Timber.e(e, "Failed to delete comment: $target")
                val message = (e as? ApiException).commentActionErrorMessage(defaultMessage = "삭제에 실패했어요.")
                if ((e as? ApiException)?.code == CommentErrorCode.NOT_FOUND) {
                    removeTarget(target, isDeleted = false, message = message)
                } else {
                    sendSideEffect(CommentRepliesSideEffect.ShowSnackbar(message = message))
                }
            }
        }
    }

    private fun handleReport() {
        val target = currentState.reportingComment ?: return
        updateState { it.copy(reportingComment = null) }
        viewModelScope.launch {
            runCatchingCancellable {
                commentRepository.report(feedId = feedId, target = target)
            }.onSuccess {
                removeTarget(target, isDeleted = false, message = "댓글 신고가 접수되었어요.")
            }.onFailure { e ->
                Timber.e(e, "Failed to report comment: $target")
                val message = (e as? ApiException).commentActionErrorMessage(defaultMessage = "신고에 실패했어요.")
                if ((e as? ApiException)?.code == CommentErrorCode.NOT_FOUND) {
                    removeTarget(target, isDeleted = false, message = message)
                } else {
                    sendSideEffect(CommentRepliesSideEffect.ShowSnackbar(message = message))
                }
            }
        }
    }

    // 원댓글이 목록에서 완전히 사라지면 이 페이지는 더 보여줄 게 없으므로 안내를 들고 상세로 돌아간다.
    // 작성자가 지운 원댓글(isDeleted)만 답글이 남았을 때 "삭제된 댓글"로 남고, 신고·이미 없음은 바로 빠진다.
    private fun removeTarget(
        target: CommentTarget,
        isDeleted: Boolean,
        message: String,
    ) {
        val comment = currentState.comment ?: return
        if (target.replyId != null) {
            val remaining = currentState.replies.filterNot { it.id == target.replyId }
            // 삭제된 원댓글은 마지막 답글이 사라지면 서버에서도 정리된다.
            if (comment.isDeleted && remaining.isEmpty()) {
                sendSideEffect(CommentRepliesSideEffect.NavigateBack(message = message))
                return
            }
            if (remaining.size != currentState.replies.size) {
                updateState {
                    it.copy(
                        comment = comment.copy(replyCount = remaining.size),
                        replies = remaining,
                        totalCount = (it.totalCount - 1).coerceAtLeast(0),
                    )
                }
            }
            sendSideEffect(CommentRepliesSideEffect.ShowSnackbar(message = message))
            return
        }
        val updated = if (isDeleted) comment.copy(replyCount = currentState.replies.size).afterDeleted() else null
        if (updated == null) {
            sendSideEffect(CommentRepliesSideEffect.NavigateBack(message = message))
            return
        }
        updateState { it.copy(comment = updated, totalCount = (it.totalCount - 1).coerceAtLeast(0)) }
        sendSideEffect(CommentRepliesSideEffect.ShowSnackbar(message = message))
    }

    companion object {
        // CommentRepliesRoute의 인자 이름과 같아야 한다.
        const val KEY_FEED_ID = "feedId"
        const val KEY_COMMENT_ID = "commentId"
    }
}
