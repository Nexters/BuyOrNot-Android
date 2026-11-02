package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.analytics.Analytics
import com.sseotdabwa.buyornot.core.analytics.AnalyticsEvent
import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.core.ui.image.toAspectRatio
import com.sseotdabwa.buyornot.core.ui.image.toImageModel
import com.sseotdabwa.buyornot.core.ui.image.toProfileImageModel
import com.sseotdabwa.buyornot.domain.exception.ApiException
import com.sseotdabwa.buyornot.domain.model.CommentErrorCode
import com.sseotdabwa.buyornot.domain.model.CommentSort
import com.sseotdabwa.buyornot.domain.model.Feed
import com.sseotdabwa.buyornot.domain.model.FeedStatus
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

    // 같은 댓글을 연달아 누르면 요청이 겹쳐 서버 상태와 화면이 엇갈리므로 응답 전에는 다시 받지 않는다.
    private val pendingLikeIds = mutableSetOf<Long>()

    // 내 공감도 저장소가 스레드 변경 신호로 다시 알려주는데, 이미 응답으로 맞춘 상태라 그 신호만큼은 다시 받지 않는다.
    private val ownLikeSignals = mutableMapOf<Long, Int>()

    init {
        observeUserPreferences()
        observeThreadChanges()
        loadDetail()
        markAsRead()
    }

    // 답글 페이지에서 바뀐 스레드를 돌아왔을 때 보던 위치를 잃지 않도록 그 스레드만 다시 받는다.
    private fun observeThreadChanges() {
        viewModelScope.launch {
            commentRepository.threadChanges.collect { change ->
                if (change.feedId != feedId) return@collect
                val ownSignals = ownLikeSignals[change.commentId] ?: 0
                if (ownSignals > 0) {
                    ownLikeSignals[change.commentId] = ownSignals - 1
                    return@collect
                }
                refreshThread(change.commentId)
            }
        }
    }

    private fun observeUserPreferences() {
        viewModelScope.launch {
            userPreferencesRepository.userPreferences
                .collect { preferences ->
                    updateState {
                        it.copy(
                            voterProfileImage = preferences.profileImage.toProfileImageModel(),
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
                // 금칙어·작성 빈도 제한으로 막힌 등록은 내용을 고치면 다시 열린다.
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
                updateState { it.copy(deletingComment = intent.target) }
            NotificationDetailIntent.DismissDeleteCommentDialog -> updateState { it.copy(deletingComment = null) }
            NotificationDetailIntent.OnDeleteCommentConfirmed -> handleDeleteComment()
            is NotificationDetailIntent.ShowReportCommentDialog ->
                updateState { it.copy(reportingComment = intent.target) }
            NotificationDetailIntent.DismissReportCommentDialog -> updateState { it.copy(reportingComment = null) }
            NotificationDetailIntent.OnReportCommentConfirmed -> handleReportComment()
            is NotificationDetailIntent.OnReplyClicked ->
                sendSideEffect(NotificationDetailSideEffect.NavigateToReplies(feedId = feedId, commentId = intent.commentId))
            is NotificationDetailIntent.OnLikeClicked -> handleLike(intent.commentId)
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
                        feed = feed.toNotificationDetailFeed(),
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
                ?.authorUserId ?: return
        val nickname =
            uiState.value.feed
                ?.authorNickname
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
                feedRepository.voteFeed(feedId, choice)
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
                // 내가 쓴 댓글에 투표 태그가 붙도록 다시 불러온다.
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
        if (!state.canViewComments || !state.hasNextComments || state.isCommentsLoading || state.isNextCommentsLoading) {
            return
        }
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
                        isCommentSubmitBlocked = apiError.blocksResubmit(),
                    )
                }
                sendSideEffect(
                    NotificationDetailSideEffect.ShowSnackbar(message = apiError.createErrorMessage(defaultMessage = "댓글 등록에 실패했어요.")),
                )
            }
        }
    }

    private fun handleDeleteComment() {
        val target = currentState.deletingComment ?: return
        updateState { it.copy(deletingComment = null) }
        viewModelScope.launch {
            runCatchingCancellable {
                commentRepository.delete(feedId = feedId, target = target)
            }.onSuccess {
                removeTarget(target, isDeleted = true)
                sendSideEffect(NotificationDetailSideEffect.ShowSnackbar(message = "삭제가 완료되었어요."))
            }.onFailure { e ->
                Timber.e(e, "Failed to delete comment: $target")
                if ((e as? ApiException)?.code == CommentErrorCode.NOT_FOUND) removeTarget(target, isDeleted = false)
                sendSideEffect(
                    NotificationDetailSideEffect.ShowSnackbar(
                        message = (e as? ApiException).commentActionErrorMessage(defaultMessage = "삭제에 실패했어요."),
                    ),
                )
            }
        }
    }

    private fun handleReportComment() {
        val target = currentState.reportingComment ?: return
        updateState { it.copy(reportingComment = null) }
        viewModelScope.launch {
            runCatchingCancellable {
                commentRepository.report(feedId = feedId, target = target)
            }.onSuccess {
                // 신고된 댓글은 서버 목록에서 빠지므로 화면에서도 바로 뺀다.
                removeTarget(target, isDeleted = false)
                sendSideEffect(NotificationDetailSideEffect.ShowSnackbar(message = "댓글 신고가 접수되었어요."))
            }.onFailure { e ->
                Timber.e(e, "Failed to report comment: $target")
                if ((e as? ApiException)?.code == CommentErrorCode.NOT_FOUND) removeTarget(target, isDeleted = false)
                sendSideEffect(
                    NotificationDetailSideEffect.ShowSnackbar(
                        message = (e as? ApiException).commentActionErrorMessage(defaultMessage = "신고에 실패했어요."),
                    ),
                )
            }
        }
    }

    // 작성자가 지운 원댓글은 답글이 남았으면 "삭제된 댓글"로 남는다.
    // 신고되거나 이미 없는 원댓글은 서버 개수에서 그 아래 답글까지 함께 빠지므로 화면 개수도 같이 줄인다.
    private fun removeTarget(
        target: CommentTarget,
        isDeleted: Boolean,
    ) {
        updateState { state ->
            val comment = state.comments.firstOrNull { it.id == target.commentId } ?: return@updateState state
            val updated =
                when {
                    target.replyId != null -> {
                        if (comment.replies.none { it.id == target.replyId }) return@updateState state
                        comment.withoutReply(target.replyId)
                    }
                    isDeleted -> comment.afterDeleted()
                    else -> null
                }
            val removedCount =
                if (target.replyId != null || isDeleted) {
                    1
                } else {
                    (if (comment.isDeleted) 0 else 1) + comment.replyCount
                }
            state.copy(
                comments = if (updated == null) state.comments - comment else state.comments.updateComment(comment.id) { updated },
                feed = state.feed?.let { feed -> feed.copy(commentCount = (feed.commentCount - removedCount).coerceAtLeast(0)) },
            )
        }
    }

    private fun handleLike(commentId: Long) {
        val comment = currentState.comments.firstOrNull { it.id == commentId } ?: return
        if (!currentState.canWriteComment || comment.isDeleted || !pendingLikeIds.add(commentId)) return
        updateState { it.copy(comments = it.comments.updateComment(commentId) { c -> c.toggledLike() }) }
        ownLikeSignals[commentId] = (ownLikeSignals[commentId] ?: 0) + 1
        viewModelScope.launch {
            runCatchingCancellable {
                commentRepository.toggleLike(feedId = feedId, comment = comment)
            }.onSuccess { like ->
                updateState { it.copy(comments = it.comments.updateComment(commentId) { c -> c.withLike(like) }) }
            }.onFailure { e ->
                Timber.e(e, "Failed to toggle like: $commentId")
                // 실패한 요청은 저장소가 신호를 보내지 않는다.
                ownLikeSignals[commentId] = ((ownLikeSignals[commentId] ?: 0) - 1).coerceAtLeast(0)
                updateState {
                    it.copy(
                        comments =
                            it.comments.updateComment(commentId) { c ->
                                c.copy(likeCount = comment.likeCount, isLiked = comment.isLiked)
                            },
                    )
                }
                sendSideEffect(NotificationDetailSideEffect.ShowSnackbar(message = (e as? ApiException).likeErrorMessage()))
            }
            pendingLikeIds.remove(commentId)
        }
    }

    // 답글 목록은 원댓글 응답에 모두 들어 있지만, 한 스레드만 다시 받을 때는 답글 API를 끝까지 넘긴다.
    private fun refreshThread(commentId: Long) {
        if (currentState.comments.none { it.id == commentId }) return
        viewModelScope.launch {
            runCatchingCancellable {
                val thread = commentRepository.getCommentThread(feedId = feedId, commentId = commentId)
                thread.comment.copy(replies = commentRepository.getAllReplies(feedId = feedId, commentId = commentId))
            }.onSuccess { refreshed ->
                updateState { state ->
                    val previous = state.comments.firstOrNull { it.id == commentId } ?: return@updateState state
                    // 응답을 기다리는 공감은 화면 값이 더 최신이다.
                    val merged =
                        if (commentId in pendingLikeIds) {
                            refreshed.copy(likeCount = previous.likeCount, isLiked = previous.isLiked)
                        } else {
                            refreshed
                        }
                    val countDiff = (refreshed.replyCount - previous.replyCount) + if (refreshed.isDeleted && !previous.isDeleted) -1 else 0
                    state.copy(
                        comments = state.comments.updateComment(commentId) { merged },
                        feed = state.feed?.let { feed -> feed.copy(commentCount = (feed.commentCount + countDiff).coerceAtLeast(0)) },
                    )
                }
            }.onFailure { e ->
                if ((e as? ApiException)?.code == CommentErrorCode.NOT_FOUND) {
                    removeTarget(CommentTarget(commentId), isDeleted = false)
                } else {
                    Timber.w(e, "Failed to refresh comment thread: $commentId")
                }
            }
        }
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
    }
}

private fun Feed.toNotificationDetailFeed(): NotificationDetailFeed =
    NotificationDetailFeed(
        feedId = feedId,
        title = title,
        content = content,
        price = price,
        category = category,
        yesCount = yesCount,
        noCount = noCount,
        totalCount = totalCount,
        feedStatus = feedStatus,
        productImages = images.map { it.image.toImageModel() },
        imageAspectRatios = images.map { it.toAspectRatio() },
        authorUserId = author.userId,
        authorNickname = author.nickname,
        authorProfileImage = author.profileImage.toProfileImageModel(),
        canBlockAuthor = !author.isGuest,
        createdAt = createdAt,
        hasVoted = hasVoted,
        myVoteChoice = myVoteChoice,
        productLink = productLink,
        commentCount = commentCount,
    )
