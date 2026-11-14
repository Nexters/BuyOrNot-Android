package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.core.ui.image.toProfileImageModel
import com.sseotdabwa.buyornot.domain.exception.ApiException
import com.sseotdabwa.buyornot.domain.model.CommentErrorCode
import com.sseotdabwa.buyornot.domain.model.CommentLike
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

    // 같은 댓글을 연달아 누르면 요청이 겹쳐 서버 상태와 화면이 엇갈리므로 응답 전에는 다시 받지 않는다.
    private val pendingLikes = mutableSetOf<CommentTarget>()

    init {
        observeUserPreferences()
        loadThread()
    }

    private fun observeUserPreferences() {
        viewModelScope.launch {
            userPreferencesRepository.userPreferences.collect { preferences ->
                updateState { it.copy(voterProfileImage = preferences.profileImage.toProfileImageModel()) }
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
            is CommentRepliesIntent.OnLikeClicked -> handleLike(intent.target)
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

    private fun handleLike(target: CommentTarget) {
        val before = currentState.likeOf(target) ?: return
        if (!pendingLikes.add(target)) return
        updateState { it.withLike(target, before.toggled()) }
        viewModelScope.launch {
            runCatchingCancellable {
                commentRepository.toggleLike(feedId = feedId, target = target, isLiked = before.isLiked)
            }.onSuccess { like ->
                updateState { it.withLike(target, like) }
            }.onFailure { e ->
                Timber.e(e, "Failed to toggle like: $target")
                updateState { it.withLike(target, before) }
                sendSideEffect(CommentRepliesSideEffect.ShowSnackbar(message = (e as? ApiException).likeErrorMessage()))
            }
            pendingLikes.remove(target)
        }
    }

    private fun handleDelete() {
        val target = currentState.deletingComment ?: return
        updateState { it.copy(deletingComment = null) }
        viewModelScope.launch {
            runCatchingCancellable {
                commentRepository.delete(feedId = feedId, target = target)
            }.onSuccess {
                removeTarget(target, message = "삭제가 완료되었어요.")
            }.onFailure { e ->
                Timber.e(e, "Failed to delete comment: $target")
                val message = (e as? ApiException).commentActionErrorMessage(defaultMessage = "삭제에 실패했어요.")
                if ((e as? ApiException)?.code == CommentErrorCode.NOT_FOUND) {
                    removeTarget(target, message = message)
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
                removeTarget(target, message = "댓글 신고가 접수되었어요.")
            }.onFailure { e ->
                Timber.e(e, "Failed to report comment: $target")
                val message = (e as? ApiException).commentActionErrorMessage(defaultMessage = "신고에 실패했어요.")
                if ((e as? ApiException)?.code == CommentErrorCode.NOT_FOUND) {
                    removeTarget(target, message = message)
                } else {
                    sendSideEffect(CommentRepliesSideEffect.ShowSnackbar(message = message))
                }
            }
        }
    }

    // 원댓글이 지워지거나 신고되면 서버가 답글까지 함께 지우므로, 더 보여줄 게 없는 이 페이지는 안내를 들고 상세로 돌아간다.
    private fun removeTarget(
        target: CommentTarget,
        message: String,
    ) {
        val comment = currentState.comment ?: return
        val replyId = target.replyId
        if (replyId == null) {
            sendSideEffect(CommentRepliesSideEffect.NavigateBack(message = message))
            return
        }
        val remaining = currentState.replies.filterNot { it.id == replyId }
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
    }

    // 고정 원댓글은 답글 목록을 따로 들고 있어 원댓글과 답글을 나눠 찾는다.
    private fun CommentRepliesUiState.likeOf(target: CommentTarget): CommentLike? {
        val replyId = target.replyId ?: return comment?.let { CommentLike(likeCount = it.likeCount, isLiked = it.isLiked) }
        return replies.firstOrNull { it.id == replyId }?.let { CommentLike(likeCount = it.likeCount, isLiked = it.isLiked) }
    }

    private fun CommentRepliesUiState.withLike(
        target: CommentTarget,
        like: CommentLike,
    ): CommentRepliesUiState {
        val replyId = target.replyId ?: return copy(comment = comment?.copy(likeCount = like.likeCount, isLiked = like.isLiked))
        return copy(replies = replies.updateReply(replyId) { it.copy(likeCount = like.likeCount, isLiked = like.isLiked) })
    }

    companion object {
        // CommentRepliesRoute의 인자 이름과 같아야 한다.
        const val KEY_FEED_ID = "feedId"
        const val KEY_COMMENT_ID = "commentId"
    }
}
