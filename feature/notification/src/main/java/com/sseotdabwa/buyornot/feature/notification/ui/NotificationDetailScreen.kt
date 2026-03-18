package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.sseotdabwa.buyornot.core.common.util.TimeUtils
import com.sseotdabwa.buyornot.core.designsystem.components.BackTopBar
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotAlertDialog
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotButtonDefaults
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotErrorView
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotSnackBarHost
import com.sseotdabwa.buyornot.core.designsystem.components.FeedCard
import com.sseotdabwa.buyornot.core.designsystem.components.ImageAspectRatio
import com.sseotdabwa.buyornot.core.designsystem.components.showBuyOrNotSnackBar
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.domain.model.Author
import com.sseotdabwa.buyornot.domain.model.Feed
import com.sseotdabwa.buyornot.domain.model.FeedCategory
import com.sseotdabwa.buyornot.domain.model.FeedStatus
import com.sseotdabwa.buyornot.domain.model.VoteChoice

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
        onIntent = viewModel::handleIntent,
    )
}

@Composable
fun NotificationDetailScreen(
    uiState: NotificationDetailUiState,
    onBackClick: () -> Unit,
    onIntent: (NotificationDetailIntent) -> Unit,
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

    Scaffold(
        snackbarHost = { BuyOrNotSnackBarHost(snackbarHostState) },
        topBar = { BackTopBar(onBackClick = onBackClick) },
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
                        CircularProgressIndicator(color = BuyOrNotTheme.colors.gray900)
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

                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                    ) {
                        FeedCard(
                            modifier = Modifier.padding(20.dp),
                            profileImageUrl = feed.author.profileImage ?: "",
                            nickname = feed.author.nickname,
                            category = feed.category.displayName,
                            createdAt = TimeUtils.formatRelativeTime(feed.createdAt),
                            content = feed.content,
                            productImageUrl = feed.viewUrl,
                            price = feed.price,
                            imageAspectRatio =
                                if (feed.imageWidth > 0 && feed.imageHeight > 0) {
                                    if (feed.imageHeight > feed.imageWidth) ImageAspectRatio.PORTRAIT else ImageAspectRatio.SQUARE
                                } else {
                                    ImageAspectRatio.SQUARE
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
                            onVote = { /* 이미 종료된 투표이기 때문에 투표 기능 미구현 */ },
                            onDeleteClick = { onIntent(NotificationDetailIntent.ShowDeleteDialog) },
                            onReportClick = { onIntent(NotificationDetailIntent.OnReportClicked) },
                            onBlockClick = { onIntent(NotificationDetailIntent.ShowBlockDialog) },
                        )
                    }
                }
            }
        }
    }
}

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
                            content = "이거 어때요? 투표 결과가 궁금해요!",
                            price = "35,000",
                            category = FeedCategory.BOOK,
                            yesCount = 80,
                            noCount = 20,
                            totalCount = 100,
                            feedStatus = FeedStatus.CLOSED,
                            s3ObjectKey = "",
                            viewUrl = "https://picsum.photos/800/800",
                            imageWidth = 800,
                            imageHeight = 800,
                            author =
                                Author(
                                    userId = 1L,
                                    nickname = "결정장애",
                                    profileImage = "https://picsum.photos/200",
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
