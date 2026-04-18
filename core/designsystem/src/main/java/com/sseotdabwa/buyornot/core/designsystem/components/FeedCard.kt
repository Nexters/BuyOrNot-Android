package com.sseotdabwa.buyornot.core.designsystem.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.sseotdabwa.buyornot.core.designsystem.R
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.shape.TopArrowBubbleShape
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
    title: String,
    content: String,
    productImageUrls: List<String>,
    price: String,
    imageAspectRatios: List<ImageAspectRatio> = listOf(ImageAspectRatio.SQUARE),
    isVoteEnded: Boolean,
    userVotedOptionIndex: Int? = null,
    buyVoteCount: Int,
    maybeVoteCount: Int,
    totalVoteCount: Int,
    onVote: (Int) -> Unit,
    isOwner: Boolean = false,
    voterProfileImageUrl: String = "",
    onDeleteClick: () -> Unit = {},
    onReportClick: () -> Unit = {},
    onBlockClick: () -> Unit = {},
    showMoreButton: Boolean = true,
    productLink: String? = null,
    onLinkClick: (url: String) -> Unit = {},
    showProductLinkTooltip: Boolean = false,
) {
    val hasVoted = userVotedOptionIndex != null
    val buyPercentage = if (totalVoteCount > 0) (buyVoteCount * 100 / totalVoteCount) else 0
    val maybePercentage = if (totalVoteCount > 0) (maybeVoteCount * 100 / totalVoteCount) else 0

    var fullScreenImageIndex by remember { mutableStateOf<Int?>(null) }
    val pagerState = rememberPagerState(pageCount = { productImageUrls.size })
    var tooltipVisible by remember(showProductLinkTooltip) { mutableStateOf(showProductLinkTooltip) }

    Column(modifier = modifier) {
        FeedCardHeader(
            profileImageUrl = profileImageUrl,
            nickname = nickname,
            category = category,
            createdAt = createdAt,
            isOwner = isOwner,
            showMoreButton = showMoreButton,
            onDeleteClick = onDeleteClick,
            onReportClick = onReportClick,
            onBlockClick = onBlockClick,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                if (title.isNotEmpty()) {
                    Text(
                        text = title,
                        modifier = Modifier.padding(horizontal = 4.dp),
                        style = BuyOrNotTheme.typography.subTitleS3SemiBold,
                        color = BuyOrNotTheme.colors.gray900,
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = content,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    style = BuyOrNotTheme.typography.bodyB4Medium,
                    color = BuyOrNotTheme.colors.gray800,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            FeedImageCarousel(
                productImageUrls = productImageUrls,
                pagerState = pagerState,
                imageAspectRatios = imageAspectRatios,
                price = price,
                productLink = productLink,
                showTooltip = tooltipVisible,
                onTooltipDismiss = { tooltipVisible = false },
                onFullscreenClick = { page -> fullScreenImageIndex = page },
                onLinkClick = onLinkClick,
            )

            Spacer(modifier = Modifier.height(12.dp))

            FeedVoteSection(
                hasVoted = hasVoted,
                isVoteEnded = isVoteEnded,
                userVotedOptionIndex = userVotedOptionIndex,
                buyPercentage = buyPercentage,
                maybePercentage = maybePercentage,
                totalVoteCount = totalVoteCount,
                voterProfileImageUrl = voterProfileImageUrl,
                onVote = onVote,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
    }

    fullScreenImageIndex?.let { index ->
        Popup(
            onDismissRequest = { fullScreenImageIndex = null },
            properties = PopupProperties(focusable = true, excludeFromSystemGesture = false),
        ) {
            FullScreenImageOverlay(
                imageUrl = productImageUrls[index],
                onDismiss = { fullScreenImageIndex = null },
            )
        }
    }
}

@Composable
private fun FeedCardHeader(
    profileImageUrl: String,
    nickname: String,
    category: String,
    createdAt: String,
    isOwner: Boolean,
    showMoreButton: Boolean,
    onDeleteClick: () -> Unit,
    onReportClick: () -> Unit,
    onBlockClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isInPreviewMode = LocalInspectionMode.current
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
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
                    style = BuyOrNotTheme.typography.bodyB7Medium,
                    color = BuyOrNotTheme.colors.gray600,
                )
            }
        }
        if (showMoreButton) {
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
                val ownerMenuItems =
                    listOf(
                        "삭제하기" to {
                            showMenu = false
                            onDeleteClick()
                        },
                    )
                val userMenuItems =
                    listOf(
                        "신고하기" to {
                            showMenu = false
                            onReportClick()
                        },
                        "차단하기" to {
                            showMenu = false
                            onBlockClick()
                        },
                    )
                if (showMenu) {
                    ActionPopup(
                        items = if (isOwner) ownerMenuItems else userMenuItems,
                        onDismiss = { showMenu = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedImageCarousel(
    productImageUrls: List<String>,
    pagerState: PagerState,
    imageAspectRatios: List<ImageAspectRatio>,
    price: String,
    productLink: String?,
    showTooltip: Boolean,
    onTooltipDismiss: () -> Unit,
    onFullscreenClick: (pageIndex: Int) -> Unit,
    onLinkClick: (url: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isInPreviewMode = LocalInspectionMode.current

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 10.dp,
            modifier = Modifier.animateContentSize(),
        ) { page ->
            val pageAspectRatio = imageAspectRatios.getOrElse(page) { ImageAspectRatio.SQUARE }

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(pageAspectRatio.ratio)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onFullscreenClick(page) },
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
                        model = productImageUrls[page],
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
                                                    Color.Transparent,
                                                    Color(0xFF191919).copy(alpha = 0.3f),
                                                ),
                                            endY = size.height,
                                            startY = size.height * 0.64f,
                                        ),
                                )
                            },
                )

                if (page == 0 && !productLink.isNullOrEmpty()) {
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 16.dp, end = 6.dp),
                        contentAlignment = Alignment.TopEnd,
                    ) {
                        LinkButton(
                            modifier = Modifier.padding(end = 10.dp),
                            onClick = { onLinkClick(productLink) },
                        )

                        if (showTooltip) {
                            // 시각적 버튼 높이(30dp) + 간격(6dp) = 36dp
                            FeedCardToolTip(
                                modifier = Modifier.padding(top = 36.dp),
                                onDismiss = onTooltipDismiss,
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.feed_card_price_format, price),
                    modifier =
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 14.dp, bottom = 16.dp),
                    color = BuyOrNotTheme.colors.gray0,
                    style = BuyOrNotTheme.typography.titleT1Bold,
                )
            }
        }
    }
}

@Composable
private fun FeedVoteSection(
    hasVoted: Boolean,
    isVoteEnded: Boolean,
    userVotedOptionIndex: Int?,
    buyPercentage: Int,
    maybePercentage: Int,
    totalVoteCount: Int,
    voterProfileImageUrl: String,
    onVote: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (hasVoted || isVoteEnded) {
                VoteProgressItem(
                    text = stringResource(R.string.feed_card_vote_buy),
                    percentage = buyPercentage / 100f,
                    percentageText = "$buyPercentage%",
                    progressBarColor = BuyOrNotTheme.colors.gray900,
                    shouldInvertTextColor = true,
                    leadingContent =
                        if (userVotedOptionIndex == 0) {
                            {
                                AsyncImage(
                                    model = voterProfileImageUrl,
                                    contentDescription = null,
                                    modifier =
                                        Modifier
                                            .height(20.dp)
                                            .width(20.dp)
                                            .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
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
                                AsyncImage(
                                    model = voterProfileImageUrl,
                                    contentDescription = null,
                                    modifier =
                                        Modifier
                                            .height(20.dp)
                                            .width(20.dp)
                                            .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                        } else {
                            null
                        },
                )
            } else {
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

        Row(verticalAlignment = Alignment.CenterVertically) {
            val statusText =
                if (isVoteEnded) {
                    stringResource(R.string.feed_card_vote_status_ended)
                } else {
                    stringResource(R.string.feed_card_vote_status_ongoing)
                }
            Text(
                text = stringResource(R.string.feed_card_vote_count_format, totalVoteCount, statusText),
                modifier = Modifier.padding(start = 6.dp),
                style = BuyOrNotTheme.typography.bodyB7Medium,
                color = BuyOrNotTheme.colors.gray600,
            )
        }
    }
}

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
                .clickable { onDismiss() },
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

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier =
                    Modifier
                        .size(if (index == currentPage) 7.dp else 5.dp)
                        .background(
                            color =
                                if (index == currentPage) {
                                    BuyOrNotTheme.colors.gray0
                                } else {
                                    BuyOrNotTheme.colors.gray0.copy(alpha = 0.5f)
                                },
                            shape = CircleShape,
                        ),
            )
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

@Composable
private fun LinkButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier.size(40.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier =
                Modifier
                    .background(
                        color = BuyOrNotTheme.colors.gray1000.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(26.dp),
                    ).clip(RoundedCornerShape(26.dp))
                    .padding(
                        horizontal = 10.dp,
                        vertical = 6.dp,
                    ).clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = BuyOrNotIcons.Link.asImageVector(),
                contentDescription = "Link",
                modifier = Modifier.size(18.dp),
                tint = BuyOrNotTheme.colors.gray0,
            )
        }
    }
}

@Composable
fun FeedCardToolTip(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
) {
    val tooltipShape =
        remember {
            TopArrowBubbleShape(
                cornerRadius = 10.dp,
                arrowWidth = 10.dp,
                arrowHeight = 5.dp,
                arrowOffsetFromRight = 30.dp,
            )
        }

    Row(
        modifier =
            modifier
                .background(
                    color = Color(0xCC3A3C3E),
                    shape = tooltipShape,
                ).clickable(onClick = onDismiss)
                .padding(top = 13.dp, bottom = 8.dp)
                .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "상품 링크를 확인해보세요!",
            style = BuyOrNotTheme.typography.bodyB5Medium,
            color = BuyOrNotTheme.colors.gray0,
        )
    }
}

@Preview(
    name = "FeedCardToolTip",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun FeedCardToolTipPreview() {
    BuyOrNotTheme {
        Box(
            modifier = Modifier.padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            FeedCardToolTip()
        }
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
            title = "립스틱 살까요?",
            content = "이 립스틱 색상 어때요? 평소에 안 바르던 색인데 도전해볼까 고민중이에요!",
            productImageUrls =
                listOf(
                    "https://picsum.photos/seed/product1/800/800",
                    "https://picsum.photos/seed/product2/800/800",
                    "https://picsum.photos/seed/product3/800/800",
                ),
            price = "35,000",
            imageAspectRatios = listOf(ImageAspectRatio.SQUARE),
            isVoteEnded = false,
            userVotedOptionIndex = userVotedOption,
            buyVoteCount = 20,
            maybeVoteCount = 10,
            totalVoteCount = 30,
            onVote = { optionIndex ->
                userVotedOption = optionIndex
            },
            onDeleteClick = {},
            onReportClick = {},
            productLink = "",
            showProductLinkTooltip = true,
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
            title = "이 원피스 어때요?",
            content = "이 원피스 4:5 비율로 보면 더 예쁜 것 같아요! 세로로 긴 옷 사진은 이 비율이 딱이에요.",
            productImageUrls =
                listOf(
                    "https://picsum.photos/seed/product2/800/1000",
                ),
            price = "89,000",
            imageAspectRatios = listOf(ImageAspectRatio.PORTRAIT),
            isVoteEnded = false,
            userVotedOptionIndex = userVotedOption,
            buyVoteCount = 45,
            maybeVoteCount = 15,
            totalVoteCount = 60,
            onVote = { optionIndex ->
                userVotedOption = optionIndex
            },
            onDeleteClick = {},
            onReportClick = {},
        )
    }
}
