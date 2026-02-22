package com.sseotdabwa.buyornot.feature.notification.ui

/**
 * 알림 상세 화면
 *
 * 사용자가 알림을 탭했을 때 해당 투표의 상세 내용을 표시하는 화면입니다.
 * 투표 종료 여부, 사용자의 투표 참여 이력, 투표 결과 등을 함께 보여줍니다.
 *
 * @param notificationId 조회할 알림의 고유 ID (서버 연동 시 사용)
 * @param onBackClick 뒤로 가기 버튼 클릭 시 호출되는 콜백
 */
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sseotdabwa.buyornot.core.common.util.TimeUtils
import com.sseotdabwa.buyornot.core.designsystem.components.BackTopBar
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotErrorView
import com.sseotdabwa.buyornot.core.designsystem.components.FeedCard
import com.sseotdabwa.buyornot.core.designsystem.components.ImageAspectRatio
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
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
fun NotificationDetailScreen(
    onBackClick: () -> Unit,
    viewModel: NotificationDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                    onRefreshClick = { viewModel.handleIntent(NotificationDetailIntent.OnRefresh) },
                )
            }

            uiState.feed != null -> {
                val feed = uiState.feed!!
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
                        onExpandClick = { /* 이미지 확대 */ },
                        onVote = { /* 알림 상세에서는 투표 기능이 제한될 수 있음 (필요 시 구현) */ },
                    )
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
            onBackClick = {},
        )
    }
}
