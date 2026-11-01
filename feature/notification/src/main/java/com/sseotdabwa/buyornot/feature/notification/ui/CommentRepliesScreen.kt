package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sseotdabwa.buyornot.core.designsystem.components.BackTopBarWithTitle
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotAlertDialog
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotButtonDefaults
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotErrorView
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotSnackBarHost
import com.sseotdabwa.buyornot.core.designsystem.components.CommentInput
import com.sseotdabwa.buyornot.core.designsystem.components.showBuyOrNotSnackBar
import com.sseotdabwa.buyornot.core.designsystem.components.toDisplayCount
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.core.ui.snackbar.LocalSnackbarState
import com.sseotdabwa.buyornot.domain.model.COMMENT_MAX_LENGTH
import com.sseotdabwa.buyornot.domain.model.Comment
import com.sseotdabwa.buyornot.domain.model.CommentAuthorType
import com.sseotdabwa.buyornot.domain.model.CommentReply
import com.sseotdabwa.buyornot.domain.model.VoteChoice
import kotlinx.coroutines.launch

@Composable
fun CommentRepliesRoute(
    onBackClick: () -> Unit,
    viewModel: CommentRepliesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val appSnackbarState = LocalSnackbarState.current

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                // 스낵바가 떠 있는 동안에도 화면 이동이 기다리지 않게 따로 띄운다.
                is CommentRepliesSideEffect.ShowSnackbar ->
                    launch { showBuyOrNotSnackBar(snackbarHostState = snackbarHostState, message = sideEffect.message) }
                is CommentRepliesSideEffect.NavigateBack -> {
                    sideEffect.message?.let(appSnackbarState::show)
                    onBackClick()
                }
            }
        }
    }

    CommentRepliesScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onIntent = viewModel::handleIntent,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
fun CommentRepliesScreen(
    uiState: CommentRepliesUiState,
    onBackClick: () -> Unit,
    onIntent: (CommentRepliesIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    if (uiState.deletingComment != null) {
        BuyOrNotAlertDialog(
            onDismissRequest = { onIntent(CommentRepliesIntent.DismissDeleteDialog) },
            title = "정말 삭제하시겠어요?",
            subText = "삭제한 댓글은 복구할 수 없어요.",
            confirmText = "삭제",
            dismissText = "취소",
            onConfirm = { onIntent(CommentRepliesIntent.OnDeleteConfirmed) },
            onDismiss = { onIntent(CommentRepliesIntent.DismissDeleteDialog) },
            confirmButtonColors = BuyOrNotButtonDefaults.destructiveButtonColors(),
        )
    }

    if (uiState.reportingComment != null) {
        BuyOrNotAlertDialog(
            onDismissRequest = { onIntent(CommentRepliesIntent.DismissReportDialog) },
            title = "이 댓글을 신고할까요?",
            subText = "신고한 내용은 운영 정책에 따라 검토돼요.",
            confirmText = "신고하기",
            dismissText = "취소",
            onConfirm = { onIntent(CommentRepliesIntent.OnReportConfirmed) },
            onDismiss = { onIntent(CommentRepliesIntent.DismissReportDialog) },
        )
    }

    val scrollState = rememberScrollState()
    LaunchedEffect(uiState.pendingScrollToLatest) {
        if (!uiState.pendingScrollToLatest) return@LaunchedEffect
        try {
            scrollState.animateScrollTo(scrollState.maxValue)
        } finally {
            onIntent(CommentRepliesIntent.OnScrollToLatestHandled)
        }
    }

    Scaffold(
        snackbarHost = { BuyOrNotSnackBarHost(snackbarHostState) },
        topBar = {
            BackTopBarWithTitle(
                title = "댓글 (${uiState.totalCount.toDisplayCount()})",
                onBackClick = onBackClick,
            )
        },
        bottomBar = {
            val comment = uiState.comment
            if (comment != null) {
                CommentInput(
                    value = uiState.replyInput,
                    onValueChange = { onIntent(CommentRepliesIntent.OnReplyInputChanged(it)) },
                    onSubmit = { onIntent(CommentRepliesIntent.OnReplySubmit) },
                    profileImageUrl = uiState.voterProfileImageUrl,
                    replyTargetNickname = comment.nickname.takeUnless { comment.isDeleted },
                    enabled = uiState.canWriteReply,
                    submitEnabled = uiState.canSubmitReply,
                    disabledPlaceholder = "삭제된 댓글에는 답글을 남길 수 없어요.",
                    maxLength = COMMENT_MAX_LENGTH,
                    modifier =
                        Modifier
                            .navigationBarsPadding()
                            .imePadding(),
                )
            }
        },
        containerColor = BuyOrNotTheme.colors.gray0,
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            val comment = uiState.comment
            when {
                uiState.isLoading ->
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = BuyOrNotTheme.colors.gray950,
                    )
                uiState.isError || comment == null ->
                    BuyOrNotErrorView(
                        modifier = Modifier.fillMaxSize(),
                        onRefreshClick = { onIntent(CommentRepliesIntent.OnRefresh) },
                    )
                else ->
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(horizontal = 20.dp, vertical = 20.dp),
                    ) {
                        FeedCommentThread(
                            comment = comment,
                            replies = uiState.replies,
                            // 입력창이 이미 이 원댓글에 답글을 다는 중이라 [답글쓰기]는 두지 않는다.
                            onReplyClick = null,
                            onLikeClick = { onIntent(CommentRepliesIntent.OnLikeClicked) },
                            onDelete = { onIntent(CommentRepliesIntent.ShowDeleteDialog(it)) },
                            onReport = { onIntent(CommentRepliesIntent.ShowReportDialog(it)) },
                        )
                    }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CommentRepliesScreenPreview() {
    BuyOrNotTheme {
        CommentRepliesScreen(
            uiState =
                CommentRepliesUiState(
                    isLoading = false,
                    comment =
                        Comment(
                            id = 1L,
                            authorType = CommentAuthorType.MEMBER,
                            nickname = "토봉이날다12456",
                            profileImage = null,
                            content = "이거 저 사봤는데 겁나 무겁고.. 그냥 그래요..",
                            isMine = false,
                            isAuthor = false,
                            voteChoice = VoteChoice.NO,
                            createdAt = "2026-09-20T10:35:17",
                            likeCount = 1,
                            isLiked = true,
                            replyCount = 1,
                        ),
                    replies =
                        listOf(
                            CommentReply(
                                id = 11L,
                                parentId = 1L,
                                authorType = CommentAuthorType.MEMBER,
                                nickname = "거북이날다12456",
                                profileImage = null,
                                content = "이거 저 사봤는데 겁나 무겁고.. 그냥 그래요.. 이거 저 사봤는데 겁나 무겁고.. 그냥 그래요..",
                                isMine = true,
                                isAuthor = false,
                                createdAt = "2026-09-20T11:35:17",
                            ),
                        ),
                    totalCount = 2,
                ),
            onBackClick = {},
            onIntent = {},
        )
    }
}
