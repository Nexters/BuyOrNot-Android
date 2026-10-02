package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.analytics.Analytics
import com.sseotdabwa.buyornot.core.analytics.AnalyticsEvent
import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.domain.exception.ApiException
import com.sseotdabwa.buyornot.domain.model.CommentErrorCode
import com.sseotdabwa.buyornot.domain.model.CommentSort
import com.sseotdabwa.buyornot.domain.model.FeedStatus
import com.sseotdabwa.buyornot.domain.model.UserType
import com.sseotdabwa.buyornot.domain.model.VoteChoice
import com.sseotdabwa.buyornot.domain.repository.CommentRepository
import com.sseotdabwa.buyornot.domain.repository.FeedRepository
import com.sseotdabwa.buyornot.domain.repository.NotificationRepository
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import com.sseotdabwa.buyornot.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NotificationDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val analytics: Analytics,
    private val feedRepository: FeedRepository,
    private val commentRepository: CommentRepository,
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : BaseViewModel<NotificationDetailUiState, NotificationDetailIntent, NotificationDetailSideEffect>(
        NotificationDetailUiState(
            pendingScrollToComments = savedStateHandle[KEY_SCROLL_TO_COMMENTS] ?: false,
            pendingFocusCommentInput = savedStateHandle[KEY_FOCUS_COMMENT_INPUT] ?: false,
        ),
    ) {
    private val notificationId: Long = savedStateHandle["notificationId"] ?: -1L
    private val feedId: Long = checkNotNull(savedStateHandle["feedId"])

    private var currentUserId: Long? = null

    // 댓글 목록 요청 세대. 정렬 변경·새로고침 시 증가시켜, 늦게 도착한 이전 목록/다음 페이지 응답이
    // 최신 목록을 덮어쓰지 않게 한다.
    private var commentGeneration = 0

    init {
        observeUserPreferences()
        loadDetail()
        markAsRead()
    }

    private fun observeUserPreferences() {
        viewModelScope.launch {
            userPreferencesRepository.userPreferences
                .collect { preferences ->
                    updateState {
                        it.copy(
                            voterProfileImageUrl = preferences.profileImageUrl,
                            isGuest = preferences.userType == UserType.GUEST,
                        )
                    }
                }
        }
    }

    override fun handleIntent(intent: NotificationDetailIntent) {
        when (intent) {
            NotificationDetailIntent.OnRefresh -> loadDetail()
            NotificationDetailIntent.ShowDeleteDialog -> updateState { it.copy(showDeleteDialog = true) }
            NotificationDetailIntent.DismissDeleteDialog -> updateState { it.copy(showDeleteDialog = false) }
            NotificationDetailIntent.OnDeleteConfirmed -> {
                updateState { it.copy(showDeleteDialog = false) }
                handleDelete()
            }
            NotificationDetailIntent.OnReportClicked -> handleReport()
            NotificationDetailIntent.ShowBlockDialog -> updateState { it.copy(showBlockDialog = true) }
            NotificationDetailIntent.DismissBlockDialog -> updateState { it.copy(showBlockDialog = false) }
            NotificationDetailIntent.OnBlockConfirmed -> handleBlockConfirmed()
            NotificationDetailIntent.OnShareClicked -> handleShareClicked()
            is NotificationDetailIntent.OnVoteClicked -> handleVote(intent.optionIndex)
            is NotificationDetailIntent.OnCommentInputChanged ->
                // 금칙어로 막힌 등록은 내용을 고치면 다시 열린다.
                updateState {
                    it.copy(
                        commentInput = intent.text,
                        isCommentSubmitBlocked = it.isCommentSubmitBlocked && it.commentInput == intent.text,
                    )
                }
            NotificationDetailIntent.OnCommentSubmit -> handleCommentSubmit()
            is NotificationDetailIntent.OnCommentSortSelected -> {
                if (intent.sort == currentState.commentSort) return
                updateState { it.copy(commentSort = intent.sort) }
                loadComments()
            }
            NotificationDetailIntent.LoadNextComments -> loadNextComments()
            is NotificationDetailIntent.ShowDeleteCommentDialog ->
                updateState { it.copy(deletingCommentId = intent.commentId) }
            NotificationDetailIntent.DismissDeleteCommentDialog -> updateState { it.copy(deletingCommentId = null) }
            NotificationDetailIntent.OnDeleteCommentConfirmed -> handleDeleteComment()
            is NotificationDetailIntent.ShowReportCommentDialog ->
                updateState { it.copy(reportingCommentId = intent.commentId) }
            NotificationDetailIntent.DismissReportCommentDialog -> updateState { it.copy(reportingCommentId = null) }
            NotificationDetailIntent.OnReportCommentConfirmed -> handleReportComment()
            NotificationDetailIntent.OnCommentScrollHandled -> updateState { it.copy(pendingScrollToComments = false) }
            NotificationDetailIntent.OnCommentFocusHandled -> updateState { it.copy(pendingFocusCommentInput = false) }
        }
    }

    private fun loadDetail() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, isError = false) }
            runCatchingCancellable {
                if (currentUserId == null) {
                    runCatchingCancellable {
                        userRepository.getMyProfile().id
                    }.onSuccess { id ->
                        currentUserId = id
                    }.onFailure {
                        Timber.w("Failed to get current user ID")
                    }
                }
                // 프로필 조회가 실패해도 작성자는 투표 없이 댓글을 봐야 하므로 로그인 때 저장한 id로 판정한다.
                val ownerId = currentUserId ?: userPreferencesRepository.userId.first().takeIf { it > 0L }
                feedRepository.getFeed(feedId) to ownerId
            }.onSuccess { (feed, ownerId) ->
                val isOwner = ownerId != null && feed.author.userId == ownerId
                updateState {
                    it.copy(
                        isLoading = false,
                        feed = feed,
                        isOwner = isOwner,
                    )
                }
                loadComments()
            }.onFailure {
                updateState { it.copy(isLoading = false, isError = true) }
            }
        }
    }

    private fun handleDelete() {
        viewModelScope.launch {
            runCatchingCancellable {
                feedRepository.deleteFeed(feedId)
            }.onSuccess {
                sendSideEffect(
                    NotificationDetailSideEffect.ShowSnackbar(
                        message = "삭제가 완료되었습니다.",
                        icon = null,
                    ),
                )
                sendSideEffect(NotificationDetailSideEffect.NavigateBack)
            }.onFailure { e ->
                Timber.e(e, "Failed to delete feed: $feedId")
                sendSideEffect(
                    NotificationDetailSideEffect.ShowSnackbar(
                        message = "삭제에 실패했습니다.",
                        icon = null,
                    ),
                )
            }
        }
    }

    private fun handleBlockConfirmed() {
        updateState { it.copy(showBlockDialog = false) }
        val userId =
            uiState.value.feed
                ?.author
                ?.userId ?: return
        val nickname =
            uiState.value.feed
                ?.author
                ?.nickname
        viewModelScope.launch {
            runCatchingCancellable {
                userRepository.blockUser(userId)
            }.onSuccess {
                sendSideEffect(
                    NotificationDetailSideEffect.ShowSnackbar(
                        message = "${nickname}님이 차단되었어요.",
                        icon = null,
                    ),
                )
                sendSideEffect(NotificationDetailSideEffect.NavigateBack)
            }.onFailure { e ->
                Timber.e(e, "Failed to block user: $userId")
                sendSideEffect(
                    NotificationDetailSideEffect.ShowSnackbar(
                        message = "차단에 실패했습니다.",
                        icon = null,
                    ),
                )
            }
        }
    }

    /**
     * 공유 의도만 기록한다. 시스템 공유 시트의 선택 결과는 앱으로 돌아오지 않아 측정할 수 없다 —
     * 실제 도달은 반대편의 `app_link_opened`로 본다.
     */
    private fun handleShareClicked() {
        analytics.track(AnalyticsEvent.ShareClicked(feedId = feedId, isOwner = uiState.value.isOwner))
    }

    private fun handleReport() {
        viewModelScope.launch {
            runCatchingCancellable {
                feedRepository.reportFeed(feedId)
            }.onSuccess {
                sendSideEffect(
                    NotificationDetailSideEffect.ShowSnackbar(
                        message = "신고가 완료되었습니다.",
                        icon = BuyOrNotIcons.CheckCircle,
                    ),
                )
            }.onFailure { e ->
                Timber.e(e, "Failed to report feed: $feedId")
                val errorMessage =
                    when {
                        e.message?.contains("400") == true -> "이미 신고한 피드이거나 본인의 피드입니다."
                        else -> "신고에 실패했습니다."
                    }
                sendSideEffect(
                    NotificationDetailSideEffect.ShowSnackbar(
                        message = errorMessage,
                        icon = null,
                    ),
                )
            }
        }
    }

    private fun handleVote(optionIndex: Int) {
        val state = currentState
        val feed = state.feed ?: return
        if (state.isVoting || state.isOwner || feed.hasVoted || feed.feedStatus == FeedStatus.CLOSED) return

        val choice = if (optionIndex == 0) VoteChoice.YES else VoteChoice.NO
        updateState { it.copy(isVoting = true) }
        viewModelScope.launch {
            runCatchingCancellable {
                if (state.isGuest) {
                    feedRepository.voteGuestFeed(feedId, choice)
                } else {
                    feedRepository.voteFeed(feedId, choice)
                }
            }.onSuccess { result ->
                updateState {
                    it.copy(
                        isVoting = false,
                        feed =
                            it.feed?.copy(
                                hasVoted = true,
                                myVoteChoice = result.choice,
                                yesCount = result.yesCount,
                                noCount = result.noCount,
                                totalCount = result.totalCount,
                            ),
                    )
                }
                analytics.track(
                    AnalyticsEvent.VoteSubmitted(
                        feedId = feedId,
                        voteChoice = choice.name,
                        feedCategory = feed.category.name,
                    ),
                )
                // 투표해야 댓글이 열린다.
                loadComments()
            }.onFailure { e ->
                Timber.e(e, "Failed to vote feed: $feedId")
                updateState { it.copy(isVoting = false) }
                sendSideEffect(NotificationDetailSideEffect.ShowSnackbar(message = "투표에 실패했습니다."))
            }
        }
    }

    private fun loadComments() {
        if (!currentState.canViewComments) return
        val generation = ++commentGeneration
        val sort = currentState.commentSort
        viewModelScope.launch {
            updateState { it.copy(isCommentsLoading = true, isNextCommentsLoading = false) }
            runCatchingCancellable {
                commentRepository.getComments(feedId = feedId, sort = sort)
            }.onSuccess { page ->
                if (generation != commentGeneration) return@onSuccess
                updateState {
                    it.copy(
                        isCommentsLoading = false,
                        comments = page.comments,
                        hasNextComments = page.hasNext,
                        nextCommentCursor = page.nextCursor,
                    )
                }
            }.onFailure { e ->
                if (generation != commentGeneration) return@onFailure
                Timber.e(e, "Failed to load comments: $feedId")
                updateState { it.copy(isCommentsLoading = false) }
                sendSideEffect(NotificationDetailSideEffect.ShowSnackbar(message = "댓글을 불러오지 못했어요."))
            }
        }
    }

    private fun loadNextComments() {
        val state = currentState
        if (!state.hasNextComments || state.isCommentsLoading || state.isNextCommentsLoading) return
        val generation = commentGeneration
        viewModelScope.launch {
            updateState { it.copy(isNextCommentsLoading = true) }
            runCatchingCancellable {
                commentRepository.getComments(
                    feedId = feedId,
                    cursor = state.nextCommentCursor,
                    sort = state.commentSort,
                )
            }.onSuccess { page ->
                if (generation != commentGeneration) return@onSuccess
                updateState {
                    it.copy(
                        isNextCommentsLoading = false,
                        // 페이지 경계에서 새 댓글이 끼면 같은 댓글이 두 번 올 수 있다.
                        comments = (it.comments + page.comments).distinctBy { comment -> comment.id },
                        hasNextComments = page.hasNext,
                        nextCommentCursor = page.nextCursor,
                    )
                }
            }.onFailure { e ->
                if (generation != commentGeneration) return@onFailure
                Timber.e(e, "Failed to load next comments: $feedId")
                updateState { it.copy(isNextCommentsLoading = false) }
            }
        }
    }

    private fun handleCommentSubmit() {
        if (!currentState.canSubmitComment) return
        val content = currentState.commentInput.trim()
        updateState { it.copy(isSubmittingComment = true) }
        viewModelScope.launch {
            runCatchingCancellable {
                commentRepository.createComment(feedId = feedId, content = content)
            }.onSuccess {
                // 등록순은 새 댓글이 마지막 페이지에 붙어 첫 페이지에 안 보일 수 있어 최신순으로 바꿔 맨 위에 보여준다.
                updateState {
                    it.copy(
                        isSubmittingComment = false,
                        commentInput = "",
                        commentSort = CommentSort.LATEST,
                        feed = it.feed?.let { feed -> feed.copy(commentCount = feed.commentCount + 1) },
                    )
                }
                sendSideEffect(NotificationDetailSideEffect.ShowSnackbar(message = "의견을 남겼어요!"))
                loadComments()
            }.onFailure { e ->
                Timber.e(e, "Failed to create comment: $feedId")
                val apiError = e as? ApiException
                updateState {
                    it.copy(
                        isSubmittingComment = false,
                        isCommentSubmitBlocked = apiError?.code == CommentErrorCode.PROFANITY,
                    )
                }
                sendSideEffect(NotificationDetailSideEffect.ShowSnackbar(message = apiError.createErrorMessage()))
            }
        }
    }

    private fun handleDeleteComment() {
        val commentId = currentState.deletingCommentId ?: return
        updateState { it.copy(deletingCommentId = null) }
        viewModelScope.launch {
            runCatchingCancellable {
                commentRepository.deleteComment(feedId = feedId, commentId = commentId)
            }.onSuccess {
                removeComment(commentId)
                sendSideEffect(NotificationDetailSideEffect.ShowSnackbar(message = "삭제가 완료되었어요."))
            }.onFailure { e ->
                Timber.e(e, "Failed to delete comment: $commentId")
                if ((e as? ApiException)?.code == CommentErrorCode.NOT_FOUND) removeComment(commentId)
                sendSideEffect(
                    NotificationDetailSideEffect.ShowSnackbar(
                        message = (e as? ApiException).commentActionErrorMessage(defaultMessage = "삭제에 실패했어요."),
                    ),
                )
            }
        }
    }

    private fun handleReportComment() {
        val commentId = currentState.reportingCommentId ?: return
        updateState { it.copy(reportingCommentId = null) }
        viewModelScope.launch {
            runCatchingCancellable {
                commentRepository.reportComment(feedId = feedId, commentId = commentId)
            }.onSuccess {
                // 신고된 댓글은 서버 목록에서 빠지므로 화면에서도 바로 뺀다.
                removeComment(commentId)
                sendSideEffect(
                    NotificationDetailSideEffect.ShowSnackbar(
                        message = "댓글 신고가 접수되었어요.",
                        icon = BuyOrNotIcons.CheckCircle,
                    ),
                )
            }.onFailure { e ->
                Timber.e(e, "Failed to report comment: $commentId")
                if ((e as? ApiException)?.code == CommentErrorCode.NOT_FOUND) removeComment(commentId)
                sendSideEffect(
                    NotificationDetailSideEffect.ShowSnackbar(
                        message = (e as? ApiException).commentActionErrorMessage(defaultMessage = "신고에 실패했어요."),
                    ),
                )
            }
        }
    }

    private fun removeComment(commentId: Long) {
        updateState { state ->
            if (state.comments.none { it.id == commentId }) return@updateState state
            state.copy(
                comments = state.comments.filterNot { it.id == commentId },
                feed = state.feed?.let { feed -> feed.copy(commentCount = (feed.commentCount - 1).coerceAtLeast(0)) },
            )
        }
    }

    // 금칙어·빈도 제한은 서버 메시지를 그대로 보여준다 (API 명세).
    private fun ApiException?.createErrorMessage(): String =
        when (this?.code) {
            CommentErrorCode.PROFANITY,
            CommentErrorCode.RATE_LIMITED,
            CommentErrorCode.PROFANITY_BLOCKED,
            -> message ?: DEFAULT_CREATE_ERROR_MESSAGE
            CommentErrorCode.CLOSED_FEED -> "마감된 투표에는 댓글을 남길 수 없어요."
            else -> DEFAULT_CREATE_ERROR_MESSAGE
        }

    private fun ApiException?.commentActionErrorMessage(defaultMessage: String): String =
        when (this?.code) {
            CommentErrorCode.NOT_FOUND -> "이미 삭제된 댓글이에요."
            CommentErrorCode.SELF_REPORT -> "내가 쓴 댓글은 신고할 수 없어요."
            CommentErrorCode.ALREADY_REPORTED -> "이미 신고한 댓글이에요."
            else -> defaultMessage
        }

    private fun markAsRead() {
        if (notificationId <= 0L) return
        viewModelScope.launch {
            runCatchingCancellable {
                notificationRepository.markAsRead(notificationId)
            }
        }
    }

    companion object {
        // NotificationDetailRoute의 인자 이름과 같아야 한다.
        const val KEY_SCROLL_TO_COMMENTS = "scrollToComments"
        const val KEY_FOCUS_COMMENT_INPUT = "focusCommentInput"
        private const val DEFAULT_CREATE_ERROR_MESSAGE = "댓글 등록에 실패했어요."
    }
}
