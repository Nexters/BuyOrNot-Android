package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sseotdabwa.buyornot.core.common.util.TimeUtils
import com.sseotdabwa.buyornot.core.designsystem.components.BackTopBar
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotErrorView
import com.sseotdabwa.buyornot.core.designsystem.components.FeedCard
import com.sseotdabwa.buyornot.core.designsystem.components.ImageAspectRatio
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.domain.model.Author
import com.sseotdabwa.buyornot.domain.model.Feed
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

    NotificationDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onIntent = viewModel::handleIntent,
    )
}

@Composable
fun NotificationDetailScreen(
    uiState: NotificationDetailUiState,
    onBackClick: () -> Unit,
    onIntent: (NotificationDetailIntent) -> Unit,
) {
    var expandedImageUrl by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(BuyOrNotTheme.colors.gray0),
        ) {
            BackTopBar(onBackClick = onBackClick)

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
                            category = feed.category,
                            createdAt = TimeUtils.formatRelativeTime(feed.createdAt),
                            content = feed.content,
                            productImageUrl = feed.viewUrl,
                            price = String.format("%,d", feed.price),
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
                            onVote = { /* 이미 종료된 투표이기 때문에 투표 기능 미구현 */ },
                        )
                    }
                }
            }
        }

        // 이미지 전체 보기 오버레이
        AnimatedVisibility(
            visible = expandedImageUrl != null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            FullScreenImageOverlay(
                imageUrl = expandedImageUrl ?: "",
                onDismiss = { expandedImageUrl = null },
            )
        }
    }
}

/**
 * 이미지 전체 보기 오버레이 컴포넌트
 */
@Composable
private fun FullScreenImageOverlay(
    imageUrl: String,
    onDismiss: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onDismiss() },
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Expanded Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )

        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 10.dp, top = 10.dp)
                    .size(40.dp)
                    .clickable { onDismiss() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = BuyOrNotIcons.Close.asImageVector(),
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
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
                            price = 35000,
                            category = "패션",
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
