package com.sseotdabwa.buyornot.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sseotdabwa.buyornot.core.designsystem.R
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

enum class ImageAspectRatio(
    val ratio: Float,
) {
    SQUARE(1f), // 1:1 비율
    PORTRAIT(4f / 5f), // 4:5 비율
}

@Composable
fun FeedCard(
    modifier: Modifier = Modifier,
    profileImageUrl: String,
    nickname: String,
    category: String,
    createdAt: String,
    content: String,
    productImageUrl: String,
    price: String, // 이미지에 있는 가격 정보 추가
    imageAspectRatio: ImageAspectRatio = ImageAspectRatio.SQUARE, // 이미지 비율 (기본값: 1:1)
    isVoteEnded: Boolean, // 투표 종료 여부
    userVotedOptionIndex: Int? = null, // 사용자가 투표한 옵션 인덱스 (null: 투표 안함, 0: 사!, 1: 애매..)
    buyVoteCount: Int,
    maybeVoteCount: Int,
    totalVoteCount: Int,
    onExpandClick: (String) -> Unit,
    onVote: (Int) -> Unit, // 투표 옵션 인덱스 (0: 사!, 1: 애매..)
    isOwner: Boolean = false, // 본인 글인지 여부
    onDeleteClick: () -> Unit = {}, // 삭제 클릭 콜백 추가
    onReportClick: () -> Unit = {}, // 신고 클릭 콜백 추가
) {
    val hasVoted = userVotedOptionIndex != null
    val buyPercentage = if (totalVoteCount > 0) (buyVoteCount * 100 / totalVoteCount) else 0
    val maybePercentage = if (totalVoteCount > 0) (maybeVoteCount * 100 / totalVoteCount) else 0
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(16.dp),
    ) {
        val isInPreviewMode = LocalInspectionMode.current
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isInPreviewMode) {
                    Box(
                        modifier =
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BuyOrNotTheme.colors.gray400),
                    )
                } else {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = null,
                        modifier =
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = nickname,
                            style = BuyOrNotTheme.typography.bodyB6Medium,
                            color = BuyOrNotTheme.colors.gray800,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = BuyOrNotIcons.ArrowRight.asImageVector(),
                            contentDescription = "Arrow Right",
                            tint = BuyOrNotTheme.colors.gray600,
                            modifier = Modifier.size(10.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = category,
                            style = BuyOrNotTheme.typography.bodyB6Medium,
                            color = BuyOrNotTheme.colors.gray800,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = createdAt,
                        style = BuyOrNotTheme.typography.captionC1Regular,
                        color = BuyOrNotTheme.colors.gray400,
                    )
                }
            }
            Box {
                Icon(
                    imageVector = BuyOrNotIcons.More.asImageVector(),
                    contentDescription = "More",
                    modifier =
                        Modifier
                            .size(20.dp)
                            .clickable { showMenu = true },
                    tint = BuyOrNotTheme.colors.gray500,
                )
                if (showMenu) {
                    FeedActionPopup(
                        label = if (isOwner) "삭제하기" else "신고하기",
                        onDismiss = { showMenu = false },
                        onClick = {
                            showMenu = false
                            if (isOwner) onDeleteClick() else onReportClick()
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. 피드 내용 & 이미지
        Column(
            modifier =
                Modifier
                    .background(
                        color = BuyOrNotTheme.colors.gray100,
                        shape = RoundedCornerShape(16.dp),
                    ).clip(RoundedCornerShape(16.dp))
                    .padding(14.dp),
        ) {
            Text(
                text = content,
                style = BuyOrNotTheme.typography.bodyB4Medium,
                color = BuyOrNotTheme.colors.gray900,
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 상품 이미지 박스
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(imageAspectRatio.ratio)
                        .clip(RoundedCornerShape(16.dp)),
            ) {
                if (isInPreviewMode) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(BuyOrNotTheme.colors.gray0),
                    )
                } else {
                    AsyncImage(
                        model = productImageUrl,
                        contentDescription = "Product Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }

                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .drawBehind {
                                drawRect(
                                    brush =
                                        Brush.verticalGradient(
                                            colors =
                                                listOf(
                                                    Color.Transparent, // 시작점 (위): 투명
                                                    Color(0xFF191919).copy(alpha = 0.3f), // 끝점 (아래): 흰색
                                                ),
                                            endY = size.height,
                                            startY = size.height * 0.64f,
                                        ),
                                )
                            },
//                            .background(
//                                brush =
//                                    Brush.verticalGradient(
//                                        colors =
//                                            listOf(
//                                                Color.Transparent, // 시작점 (위): 투명
//                                                Color(0xFF191919) // 끝점 (아래): 흰색
//                                            ),
//                                        startY = 0f,
//                                        endY = gradientEndY
//
//                                    ),
//                            ),
                )

                // 이미지 확장 버튼 (원본 크기)
                FullscreenButton(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(
                                top = 14.dp,
                                end = 14.dp,
                            ),
                    onClick = { onExpandClick(productImageUrl) },
                )

                // 가격 태그 (좌측 하단)
                Text(
                    text = stringResource(R.string.feed_card_price_format, price),
                    modifier =
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(
                                start = 14.dp,
                                bottom = 16.dp,
                            ),
                    color = BuyOrNotTheme.colors.gray0,
                    style = BuyOrNotTheme.typography.titleT1Bold,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. 투표 영역 (VoteOption 또는 VoteProgressItem)
            // 사용자가 투표했거나 투표가 종료되었으면 결과 표시
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (hasVoted || isVoteEnded) {
                    // 투표 완료 또는 종료: VoteProgressItem으로 결과 표시
                    VoteProgressItem(
                        text = stringResource(R.string.feed_card_vote_buy),
                        percentage = buyPercentage / 100f,
                        percentageText = "$buyPercentage%",
                        progressBarColor = BuyOrNotTheme.colors.gray900,
                        shouldInvertTextColor = true,
                        leadingContent =
                            if (userVotedOptionIndex == 0) {
                                {
                                    Box(
                                        modifier =
                                            Modifier
                                                .height(20.dp)
                                                .width(20.dp)
                                                .background(
                                                    color = BuyOrNotTheme.colors.gray500,
                                                    shape = RoundedCornerShape(10.dp),
                                                ),
                                    )
                                }
                            } else {
                                null
                            },
                    )
                    VoteProgressItem(
                        text = stringResource(R.string.feed_card_vote_maybe),
                        percentage = maybePercentage / 100f,
                        percentageText = "$maybePercentage%",
                        textColor = BuyOrNotTheme.colors.gray700,
                        percentageTextColor = BuyOrNotTheme.colors.gray700,
                        leadingContent =
                            if (userVotedOptionIndex == 1) {
                                {
                                    Box(
                                        modifier =
                                            Modifier
                                                .height(20.dp)
                                                .width(20.dp)
                                                .background(
                                                    color = BuyOrNotTheme.colors.gray500,
                                                    shape = RoundedCornerShape(10.dp),
                                                ),
                                    )
                                }
                            } else {
                                null
                            },
                    )
                } else {
                    // 투표 진행중: VoteOption으로 투표 가능
                    VoteOption(
                        text = stringResource(R.string.feed_card_vote_buy),
                        onClick = { onVote(0) },
                    )
                    VoteOption(
                        text = stringResource(R.string.feed_card_vote_maybe),
                        onClick = { onVote(1) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 6. 하단 상태 정보
            Row(verticalAlignment = Alignment.CenterVertically) {
                val statusText =
                    if (isVoteEnded) {
                        stringResource(R.string.feed_card_vote_status_ended)
                    } else {
                        stringResource(R.string.feed_card_vote_status_ongoing)
                    }
                Text(
                    text = stringResource(R.string.feed_card_vote_count_format, totalVoteCount, statusText),
                    style = BuyOrNotTheme.typography.captionC1Regular,
                    color = BuyOrNotTheme.colors.gray400,
                )
            }
        }
    }
}

@Composable
private fun VoteOption(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = BuyOrNotTheme.colors.gray0,
        border =
            BorderStroke(
                width = 1.dp,
                color = BuyOrNotTheme.colors.gray300,
            ),
        onClick = onClick,
    ) {
        Text(
            text = text,
            modifier =
                Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 14.dp,
                ),
            style = BuyOrNotTheme.typography.subTitleS4SemiBold,
            color = BuyOrNotTheme.colors.gray900,
        )
    }
}

/**
 * 피드 더보기 팝업 UI
 */
@Composable
private fun FeedActionPopup(
    label: String,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.width(80.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
    ) {
        Box(
            modifier =
                Modifier
                    .clickable { onClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = BuyOrNotTheme.typography.bodyB4Medium,
                color = if (label == "삭제하기") Color(0xFFF04438) else BuyOrNotTheme.colors.gray900,
            )
        }
    }
}

@Composable
private fun FullscreenButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            modifier
                .size(30.dp)
                .background(
                    color = BuyOrNotTheme.colors.gray1000.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                ).clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = BuyOrNotIcons.Expand.asImageVector(),
            contentDescription = "Fullscreen",
            modifier = Modifier.size(18.dp),
            tint = BuyOrNotTheme.colors.gray300,
        )
    }
}

@Preview(
    name = "FeedCard - Square (1:1) Interactive",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun FeedCardSquareInteractivePreview() {
    BuyOrNotTheme {
        var userVotedOption by remember { mutableStateOf<Int?>(null) }

        FeedCard(
            profileImageUrl = "https://picsum.photos/seed/profile1/200/200",
            nickname = "결정장애",
            category = "뷰티",
            createdAt = "10분 전",
            content = "이 립스틱 색상 어때요? 평소에 안 바르던 색인데 도전해볼까 고민중이에요!",
            productImageUrl = "https://picsum.photos/seed/product1/800/800",
            price = "35,000",
            imageAspectRatio = ImageAspectRatio.SQUARE,
            isVoteEnded = false,
            userVotedOptionIndex = userVotedOption,
            buyVoteCount = 20,
            maybeVoteCount = 10,
            totalVoteCount = 30,
            onExpandClick = { },
            onVote = { optionIndex ->
                userVotedOption = optionIndex
            },
            onDeleteClick = {},
            onReportClick = {},
        )
    }
}

@Preview(
    name = "FeedCard - Portrait (4:5) Interactive",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun FeedCardPortraitInteractivePreview() {
    BuyOrNotTheme {
        var userVotedOption by remember { mutableStateOf<Int?>(null) }

        FeedCard(
            profileImageUrl = "https://picsum.photos/seed/profile2/200/200",
            nickname = "패션피플",
            category = "의류",
            createdAt = "2시간 전",
            content = "이 원피스 4:5 비율로 보면 더 예쁜 것 같아요! 세로로 긴 옷 사진은 이 비율이 딱이에요.",
            productImageUrl = "https://picsum.photos/seed/product2/800/1000",
            price = "89,000",
            imageAspectRatio = ImageAspectRatio.PORTRAIT,
            isVoteEnded = false,
            userVotedOptionIndex = userVotedOption,
            buyVoteCount = 45,
            maybeVoteCount = 15,
            totalVoteCount = 60,
            onExpandClick = { },
            onVote = { optionIndex ->
                userVotedOption = optionIndex
            },
            onDeleteClick = {},
            onReportClick = {},
        )
    }
}
