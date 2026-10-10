package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sseotdabwa.buyornot.core.common.util.TimeUtils
import com.sseotdabwa.buyornot.core.designsystem.components.BackTopBar
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotAlertDialog
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotButtonDefaults
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotErrorView
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotSnackBarHost
import com.sseotdabwa.buyornot.core.designsystem.components.CommentInput
import com.sseotdabwa.buyornot.core.designsystem.components.FeedCard
import com.sseotdabwa.buyornot.core.designsystem.components.ImageAspectRatio
import com.sseotdabwa.buyornot.core.designsystem.components.showBuyOrNotSnackBar
import com.sseotdabwa.buyornot.core.designsystem.preview.PreviewImages
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.domain.model.Author
import com.sseotdabwa.buyornot.domain.model.COMMENT_MAX_LENGTH
import com.sseotdabwa.buyornot.domain.model.Comment
import com.sseotdabwa.buyornot.domain.model.CommentAuthorType
import com.sseotdabwa.buyornot.domain.model.Feed
import com.sseotdabwa.buyornot.domain.model.FeedCategory
import com.sseotdabwa.buyornot.domain.model.FeedImage
import com.sseotdabwa.buyornot.domain.model.FeedStatus
import com.sseotdabwa.buyornot.domain.model.VoteChoice
import kotlinx.coroutines.launch

/** 마지막 댓글과 댓글 입력 바 사이 여백. */
private val CommentListBottomSpace = 60.dp

/**
 * 알림 상세 화면
 *
 * 사용자가 알림을 탭했을 때 해당 투표의 상세 내용을 표시하는 화면입니다.
 * 투표 종료 여부, 사용자의 투표 참여 이력, 투표 결과 등을 함께 보여줍니다.
 *
 * @param onBackClick 뒤로 가기 버튼 클릭 시 호출되는 콜백
 * @param viewModel 알림 상세 ViewModel
 */
@Composable
fun NotificationDetailRoute(
    onBackClick: () -> Unit,
    onLinkClick: (url: String) -> Unit = {},
    onShareClick: (feedId: Long, title: String) -> Unit = { _, _ -> },
    onImageClick: (imageUrls: List<String>, page: Int) -> Unit = { _, _ -> },
    viewModel: NotificationDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // SideEffect 처리
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is NotificationDetailSideEffect.ShowSnackbar -> {
                    showBuyOrNotSnackBar(
                        snackbarHostState = snackbarHostState,
                        message = sideEffect.message,
                        iconResource = sideEffect.icon,
                    )
                }
                NotificationDetailSideEffect.NavigateBack -> onBackClick()
            }
        }
    }

    NotificationDetailScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onLinkClick = onLinkClick,
        onShareClick = onShareClick,
        onImageClick = onImageClick,
        onIntent = viewModel::handleIntent,
    )
}

@Composable
fun NotificationDetailScreen(
    uiState: NotificationDetailUiState,
    onBackClick: () -> Unit,
    onIntent: (NotificationDetailIntent) -> Unit,
    onLinkClick: (url: String) -> Unit = {},
    onShareClick: (feedId: Long, title: String) -> Unit = { _, _ -> },
    onImageClick: (imageUrls: List<String>, page: Int) -> Unit = { _, _ -> },
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    if (uiState.showBlockDialog) {
        BuyOrNotAlertDialog(
            onDismissRequest = { onIntent(NotificationDetailIntent.DismissBlockDialog) },
            title = "이 글의 사용자를 차단하시겠어요?",
            subText = "${uiState.feed?.author?.nickname}님의 투표를 볼 수 없어요.",
            confirmText = "차단하기",
            dismissText = "취소",
            onConfirm = { onIntent(NotificationDetailIntent.OnBlockConfirmed) },
            onDismiss = { onIntent(NotificationDetailIntent.DismissBlockDialog) },
        )
    }

    if (uiState.showDeleteDialog) {
        BuyOrNotAlertDialog(
            onDismissRequest = { onIntent(NotificationDetailIntent.DismissDeleteDialog) },
            title = "정말 삭제하시겠어요?",
            subText = "투표 데이터가 모두 사라지며, 복구할 수 없어요.",
            confirmText = "삭제",
            dismissText = "취소",
            onConfirm = { onIntent(NotificationDetailIntent.OnDeleteConfirmed) },
            onDismiss = { onIntent(NotificationDetailIntent.DismissDeleteDialog) },
            confirmButtonColors = BuyOrNotButtonDefaults.destructiveButtonColors(),
        )
    }

    if (uiState.deletingCommentId != null) {
        BuyOrNotAlertDialog(
            onDismissRequest = { onIntent(NotificationDetailIntent.DismissDeleteCommentDialog) },
            title = "정말 삭제하시겠어요?",
            subText = "삭제한 댓글은 복구할 수 없어요.",
            confirmText = "삭제",
            dismissText = "취소",
            onConfirm = { onIntent(NotificationDetailIntent.OnDeleteCommentConfirmed) },
            onDismiss = { onIntent(NotificationDetailIntent.DismissDeleteCommentDialog) },
            confirmButtonColors = BuyOrNotButtonDefaults.destructiveButtonColors(),
        )
    }

    if (uiState.reportingCommentId != null) {
        BuyOrNotAlertDialog(
            onDismissRequest = { onIntent(NotificationDetailIntent.DismissReportCommentDialog) },
            title = "이 댓글을 신고할까요?",
            subText = "신고한 내용은 운영 정책에 따라 검토돼요.",
            confirmText = "신고하기",
            dismissText = "취소",
            onConfirm = { onIntent(NotificationDetailIntent.OnReportCommentConfirmed) },
            onDismiss = { onIntent(NotificationDetailIntent.DismissReportCommentDialog) },
        )
    }

    val listState = rememberLazyListState()
    val commentFocusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    val commentHeaderIndex = 1
    val scrollToComments: () -> Unit = {
        coroutineScope.launch { listState.animateScrollToItem(commentHeaderIndex) }
    }

    CommentEntryEffects(
        uiState = uiState,
        listState = listState,
        commentHeaderIndex = commentHeaderIndex,
        focusRequester = commentFocusRequester,
        onIntent = onIntent,
    )

    Scaffold(
        snackbarHost = { BuyOrNotSnackBarHost(snackbarHostState) },
        topBar = { BackTopBar(onBackClick = onBackClick) },
        bottomBar = {
            val feed = uiState.feed
            if (!uiState.isLoading && !uiState.isError && feed != null) {
                CommentInput(
                    value = uiState.commentInput,
                    onValueChange = { onIntent(NotificationDetailIntent.OnCommentInputChanged(it)) },
                    onSubmit = { onIntent(NotificationDetailIntent.OnCommentSubmit) },
                    profileImageUrl = uiState.voterProfileImageUrl,
                    votedOptionLabel = feed.myVoteChoice?.label(),
                    enabled = uiState.canWriteComment,
                    submitEnabled = uiState.canSubmitComment,
                    // 투표 전·마감 피드는 시안대로 기본 문구를 흐리게 두고, 이유는 댓글 영역 안내 문구로 알린다.
                    disabledPlaceholder = if (uiState.isGuest) "로그인 후 의견을 작성할 수 있어요." else "댓글을 남겨주세요!",
                    maxLength = COMMENT_MAX_LENGTH,
                    focusRequester = commentFocusRequester,
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
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = BuyOrNotTheme.colors.gray950)
                    }
                }

                uiState.isError -> {
                    BuyOrNotErrorView(
                        modifier = Modifier.fillMaxSize(),
                        onRefreshClick = { onIntent(NotificationDetailIntent.OnRefresh) },
                    )
                }

                uiState.feed != null -> {
                    val feed = uiState.feed
                    var showLinkTooltip by remember { mutableStateOf(true) }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = CommentListBottomSpace - CommentItemVerticalPadding),
                    ) {
                        item(key = "feed") {
                            FeedCard(
                                modifier = Modifier.padding(top = 26.dp, bottom = 16.dp),
                                profileImageUrl = feed.author.profileImage ?: "",
                                nickname = feed.author.nickname,
                                category = feed.category.displayName,
                                createdAt = TimeUtils.formatRelativeTime(feed.createdAt),
                                title = feed.title,
                                content = feed.content,
                                productImageUrls = feed.viewUrls,
                                price = feed.price,
                                imageAspectRatios =
                                    feed.images.map { image ->
                                        when {
                                            image.imageWidth > image.imageHeight -> ImageAspectRatio.LANDSCAPE
                                            image.imageWidth < image.imageHeight -> ImageAspectRatio.PORTRAIT
                                            else -> ImageAspectRatio.SQUARE
                                        }
                                    },
                                isVoteEnded = feed.feedStatus == FeedStatus.CLOSED,
                                userVotedOptionIndex =
                                    when (feed.myVoteChoice) {
                                        VoteChoice.YES -> 0
                                        VoteChoice.NO -> 1
                                        null -> null
                                    },
                                buyVoteCount = feed.yesCount,
                                maybeVoteCount = feed.noCount,
                                totalVoteCount = feed.totalCount,
                                isOwner = uiState.isOwner,
                                voterProfileImageUrl = uiState.voterProfileImageUrl,
                                onVote = { optionIndex -> onIntent(NotificationDetailIntent.OnVoteClicked(optionIndex)) },
                                onDeleteClick = { onIntent(NotificationDetailIntent.ShowDeleteDialog) },
                                onReportClick = { onIntent(NotificationDetailIntent.OnReportClicked) },
                                onBlockClick = { onIntent(NotificationDetailIntent.ShowBlockDialog) },
                                onShareClick = {
                                    onIntent(NotificationDetailIntent.OnShareClicked)
                                    onShareClick(feed.feedId, feed.title)
                                },
                                canBlock = !feed.author.isGuest,
                                showMoreButton = !uiState.isGuest,
                                productLink = feed.productLink,
                                onLinkClick = onLinkClick,
                                showProductLinkTooltip = showLinkTooltip && feed.productLink != null,
                                onTooltipDismiss = { showLinkTooltip = false },
                                onImageClick = onImageClick,
                                commentCount = feed.commentCount,
                                onCommentClick = scrollToComments,
                            )
                        }

                        commentSection(uiState = uiState, onIntent = onIntent)
                    }

                    LoadNextCommentsEffect(
                        listState = listState,
                        commentCount = uiState.comments.size,
                        onIntent = onIntent,
                    )
                }
            }
        }
    }
}

/**
 * 댓글 진입점으로 들어왔을 때 댓글 영역으로 스크롤하고, [의견 남기기]로 들어왔으면 입력창에 포커스한다.
 * 피드가 그려진 뒤에만 스크롤할 수 있으므로 피드 로딩을 기다린다.
 */
@Composable
private fun CommentEntryEffects(
    uiState: NotificationDetailUiState,
    listState: LazyListState,
    commentHeaderIndex: Int,
    focusRequester: FocusRequester,
    onIntent: (NotificationDetailIntent) -> Unit,
) {
    val isFeedShown = uiState.feed != null && !uiState.isLoading
    val currentUiState by rememberUpdatedState(uiState)
    // 스크롤 두 번이 서로를 취소하지 않도록 한 effect에서 처리하고, 중간에 취소돼도 다시 실행되지 않게 finally에서 완료 처리한다.
    LaunchedEffect(isFeedShown) {
        if (!isFeedShown) return@LaunchedEffect
        val state = currentUiState
        val shouldFocus = state.pendingFocusCommentInput && state.canWriteComment
        if (!state.pendingScrollToComments && !shouldFocus) {
            if (state.pendingFocusCommentInput) onIntent(NotificationDetailIntent.OnCommentFocusHandled)
            return@LaunchedEffect
        }
        try {
            listState.animateScrollToItem(commentHeaderIndex)
            if (shouldFocus) focusRequester.requestFocus()
        } finally {
            onIntent(NotificationDetailIntent.OnCommentScrollHandled)
            onIntent(NotificationDetailIntent.OnCommentFocusHandled)
        }
    }
}

/**
 * 끝에서 [COMMENT_PREFETCH_DISTANCE]개 안쪽까지 스크롤하면 다음 댓글 페이지를 부른다.
 * 끝에 머문 채 목록이 새로 채워지면 [shouldLoadMore]가 바뀌지 않으므로 [commentCount]로도 다시 확인한다.
 */
@Composable
private fun LoadNextCommentsEffect(
    listState: LazyListState,
    commentCount: Int,
    onIntent: (NotificationDetailIntent) -> Unit,
) {
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            lastVisible >= layoutInfo.totalItemsCount - COMMENT_PREFETCH_DISTANCE
        }
    }
    LaunchedEffect(shouldLoadMore, commentCount) {
        if (shouldLoadMore) onIntent(NotificationDetailIntent.LoadNextComments)
    }
}

private const val COMMENT_PREFETCH_DISTANCE = 3

@Preview(showBackground = true)
@Composable
private fun NotificationDetailScreenPreview() {
    BuyOrNotTheme {
        NotificationDetailScreen(
            uiState =
                NotificationDetailUiState(
                    isLoading = false,
                    feed =
                        Feed(
                            feedId = 1L,
                            title = "",
                            content = "이거 어때요? 투표 결과가 궁금해요!",
                            price = "35,000",
                            category = FeedCategory.BOOK,
                            yesCount = 80,
                            noCount = 20,
                            totalCount = 100,
                            feedStatus = FeedStatus.CLOSED,
                            images =
                                listOf(
                                    FeedImage(
                                        s3ObjectKey = "",
                                        imageUrl = PreviewImages.square(),
                                        imageWidth = 800,
                                        imageHeight = 800,
                                    ),
                                ),
                            author =
                                Author(
                                    userId = 1L,
                                    nickname = "결정장애",
                                    profileImage = PreviewImages.avatar(),
                                ),
                            createdAt = "2026-02-21T15:00:53.552Z",
                            hasVoted = true,
                            myVoteChoice = VoteChoice.YES,
                        ),
                ),
            onBackClick = {},
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun NotificationDetailScreenWithCommentsPreview() {
    BuyOrNotTheme {
        NotificationDetailScreen(
            uiState =
                NotificationDetailUiState(
                    isLoading = false,
                    feed =
                        Feed(
                            feedId = 1L,
                            title = "장화 살지말지 고민됩니다",
                            content = "장마가 이미 끝나버리긴 했는데 지금 할인기간이라 매우 고민됩니다..",
                            price = "31,900",
                            category = FeedCategory.ETC,
                            yesCount = 3,
                            noCount = 1,
                            totalCount = 4,
                            feedStatus = FeedStatus.OPEN,
                            images =
                                listOf(
                                    FeedImage(
                                        s3ObjectKey = "",
                                        imageUrl = PreviewImages.square(),
                                        imageWidth = 800,
                                        imageHeight = 800,
                                    ),
                                ),
                            author = Author(userId = 2L, nickname = "참새방앗간12456", profileImage = null),
                            createdAt = "2026-09-20T10:35:17",
                            hasVoted = true,
                            myVoteChoice = VoteChoice.YES,
                            commentCount = 3,
                        ),
                    comments =
                        listOf(
                            previewComment(1, voteChoice = VoteChoice.YES),
                            previewComment(2, voteChoice = VoteChoice.NO, isMine = true),
                            previewComment(3, voteChoice = null, isAuthor = true),
                            previewComment(4, voteChoice = null, authorType = CommentAuthorType.GUEST),
                        ),
                ),
            onBackClick = {},
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun NotificationDetailScreenBeforeVotePreview() {
    BuyOrNotTheme {
        NotificationDetailScreen(
            uiState =
                NotificationDetailUiState(
                    isLoading = false,
                    feed =
                        Feed(
                            feedId = 1L,
                            title = "장화 살지말지 고민됩니다",
                            content = "장마가 이미 끝나버리긴 했는데 지금 할인기간이라 매우 고민됩니다..",
                            price = "31,900",
                            category = FeedCategory.ETC,
                            yesCount = 3,
                            noCount = 1,
                            totalCount = 4,
                            feedStatus = FeedStatus.OPEN,
                            images =
                                listOf(
                                    FeedImage(
                                        s3ObjectKey = "",
                                        imageUrl = PreviewImages.square(),
                                        imageWidth = 800,
                                        imageHeight = 800,
                                    ),
                                ),
                            author = Author(userId = 2L, nickname = "참새방앗간12456", profileImage = null),
                            createdAt = "2026-09-20T10:35:17",
                            hasVoted = false,
                            myVoteChoice = null,
                            commentCount = 12,
                        ),
                ),
            onBackClick = {},
            onIntent = {},
        )
    }
}

private fun previewComment(
    id: Long,
    voteChoice: VoteChoice?,
    isMine: Boolean = false,
    isAuthor: Boolean = false,
    authorType: CommentAuthorType = CommentAuthorType.MEMBER,
) = Comment(
    id = id,
    authorType = authorType,
    nickname = if (authorType == CommentAuthorType.GUEST) "지름신들린수달_1234" else "거북이날다1245$id",
    profileImage = null,
    content = "이거 저 사봤는데 겁나 무겁고.. 그냥 그래요..",
    isMine = isMine,
    isAuthor = isAuthor,
    voteChoice = voteChoice,
    createdAt = "2026-09-20T10:35:17",
)
