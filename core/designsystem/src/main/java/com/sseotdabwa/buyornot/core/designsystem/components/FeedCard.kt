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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sseotdabwa.buyornot.core.designsystem.R
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.preview.PreviewImages
import com.sseotdabwa.buyornot.core.designsystem.shape.TopArrowBubbleShape
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.core.designsystem.util.nonRippleClickable

/** 피드 카드 하단에 보여줄 최신 댓글 미리보기. */
data class FeedCommentPreview(
    val nickname: String,
    val content: String,
)

private val CardHorizontalPadding = 20.dp
private val ProfileAvatarSize = 32.dp
private val ProfileNicknameSpacing = 10.dp

// thread 레이아웃에서는 본문을 닉네임 시작점에 맞춰 들여쓴다.
private val ThreadContentStart = CardHorizontalPadding + ProfileAvatarSize + ProfileNicknameSpacing

// thread 라인은 프로필 아바타 중심에서 내려온다.
private val ThreadLineX = CardHorizontalPadding + ProfileAvatarSize / 2
private val ThreadLineWidth = 1.2.dp
private val ThreadCornerRadius = 10.dp

// 라인이 꺾여 미리보기 카드 왼쪽 끝에 붙는 높이 (미리보기 카드 상단 기준).
private val ThreadJoinOffsetY = 35.dp

private val ProductImageCornerRadius = 14.dp
private val ProductImageSpacing = 10.dp

// thread 레이아웃은 들여쓴 만큼 이미지가 좁아져 다음 이미지가 보이도록 간격을 줄인다.
private val ThreadProductImageSpacing = 8.dp
private val PriceTextPadding = 16.dp

enum class ImageAspectRatio(
    val ratio: Float,
) {
    SQUARE(1f), // 1:1 비율 (가로 == 세로)
    LANDSCAPE(5f / 4f), // 5:4 비율 (가로 > 세로)
    PORTRAIT(4f / 5f), // 4:5 비율 (가로 < 세로)
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
    onShareClick: () -> Unit = {},
    canBlock: Boolean = true,
    showMoreButton: Boolean = true,
    productLink: String? = null,
    onLinkClick: (url: String) -> Unit = {},
    showProductLinkTooltip: Boolean = false,
    onTooltipDismiss: () -> Unit = {},
    onImageClick: (imageUrls: List<String>, page: Int) -> Unit = { _, _ -> },
    useThreadLayout: Boolean = false,
    commentCount: Int? = null,
    latestComment: FeedCommentPreview? = null,
    onCommentClick: () -> Unit = {},
) {
    val hasVoted = userVotedOptionIndex != null
    val buyPercentage = if (totalVoteCount > 0) (buyVoteCount * 100 / totalVoteCount) else 0
    val maybePercentage = if (totalVoteCount > 0) (maybeVoteCount * 100 / totalVoteCount) else 0

    val pagerState = rememberPagerState(pageCount = { productImageUrls.size })
    var tooltipVisible by remember(showProductLinkTooltip) { mutableStateOf(showProductLinkTooltip) }

    val contentStart = if (useThreadLayout) ThreadContentStart else CardHorizontalPadding
    val showThreadLine = useThreadLayout && latestComment != null
    val threadLineColor = BuyOrNotTheme.colors.gray300
    // 라인이 헤더 아바타와 미리보기 카드를 잇도록 두 위치를 측정해 카드 전체 위에 그린다.
    var headerHeightPx by remember { mutableFloatStateOf(0f) }
    var previewTopPx by remember { mutableFloatStateOf(0f) }

    Column(
        modifier =
            modifier.drawBehind {
                if (showThreadLine && previewTopPx > 0f) {
                    val x = ThreadLineX.toPx()
                    // 아바타는 헤더 행 안에서 세로 가운데 정렬된다.
                    val startY = (headerHeightPx + ProfileAvatarSize.toPx()) / 2f
                    val joinY = previewTopPx + ThreadJoinOffsetY.toPx()
                    val radius = ThreadCornerRadius.toPx()
                    val path =
                        Path().apply {
                            moveTo(x, startY)
                            lineTo(x, joinY - radius)
                            quadraticTo(x, joinY, x + radius, joinY)
                            lineTo(contentStart.toPx(), joinY)
                        }
                    drawPath(
                        path = path,
                        color = threadLineColor,
                        style = Stroke(width = ThreadLineWidth.toPx(), cap = StrokeCap.Round),
                    )
                }
            },
    ) {
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
            onShareClick = onShareClick,
            canBlock = canBlock,
            modifier = Modifier.onSizeChanged { headerHeightPx = it.height.toFloat() },
        )

        Column {
            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.padding(start = contentStart, end = CardHorizontalPadding)) {
                if (title.isNotEmpty()) {
                    Text(
                        text = title,
                        modifier = Modifier.padding(horizontal = 4.dp),
                        style = BuyOrNotTheme.typography.subTitleS3SemiBold,
                        color = BuyOrNotTheme.colors.gray950,
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = content,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    style = BuyOrNotTheme.typography.paragraphP3Medium,
                    color = BuyOrNotTheme.colors.gray800,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FeedImageCarousel(
                productImageUrls = productImageUrls,
                pagerState = pagerState,
                imageAspectRatios = imageAspectRatios,
                price = price,
                productLink = productLink,
                showTooltip = tooltipVisible,
                onTooltipDismiss = {
                    tooltipVisible = false
                    onTooltipDismiss()
                },
                onFullscreenClick = { page -> onImageClick(productImageUrls, page) },
                onLinkClick = onLinkClick,
                contentPadding = PaddingValues(start = contentStart, end = CardHorizontalPadding),
                pageSpacing = if (useThreadLayout) ThreadProductImageSpacing else ProductImageSpacing,
                priceStyle =
                    if (useThreadLayout) {
                        BuyOrNotTheme.typography.headingH4Bold
                    } else {
                        BuyOrNotTheme.typography.titleT1Bold
                    },
            )

            Spacer(modifier = Modifier.height(12.dp))

            FeedVoteSection(
                hasVoted = hasVoted,
                isVoteEnded = isVoteEnded,
                isOwner = isOwner,
                userVotedOptionIndex = userVotedOptionIndex,
                buyPercentage = buyPercentage,
                maybePercentage = maybePercentage,
                totalVoteCount = totalVoteCount,
                voterProfileImageUrl = voterProfileImageUrl,
                onVote = onVote,
                commentCount = commentCount,
                onCommentClick = onCommentClick,
                modifier = Modifier.padding(start = contentStart, end = CardHorizontalPadding),
            )
        }

        if (latestComment != null) {
            Spacer(modifier = Modifier.height(14.dp))
            CommentPreviewCard(
                nickname = latestComment.nickname,
                content = latestComment.content,
                modifier =
                    Modifier
                        .onPlaced { previewTopPx = it.positionInParent().y }
                        .padding(start = contentStart, end = CardHorizontalPadding)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onCommentClick),
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
    onShareClick: () -> Unit,
    canBlock: Boolean,
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
                            .size(ProfileAvatarSize)
                            .clip(CircleShape)
                            .background(BuyOrNotTheme.colors.gray400),
                )
            } else {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .size(ProfileAvatarSize)
                            .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            }
            Spacer(modifier = Modifier.width(ProfileNicknameSpacing))
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
                Spacer(modifier = Modifier.height(3.dp))
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
                val shareMenuItem =
                    "공유하기" to {
                        showMenu = false
                        onShareClick()
                    }
                val ownerMenuItems =
                    listOf(
                        shareMenuItem,
                        "삭제하기" to {
                            showMenu = false
                            onDeleteClick()
                        },
                    )
                // 비회원 글은 차단할 대상(유저)이 없어 항목을 아예 노출하지 않는다.
                // 신고는 유저가 아니라 피드를 대상으로 하므로 그대로 남긴다.
                val userMenuItems =
                    listOfNotNull(
                        shareMenuItem,
                        "신고하기" to {
                            showMenu = false
                            onReportClick()
                        },
                        if (canBlock) {
                            "차단하기" to {
                                showMenu = false
                                onBlockClick()
                            }
                        } else {
                            null
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
    priceStyle: TextStyle,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp),
    pageSpacing: Dp = ProductImageSpacing,
) {
    val isInPreviewMode = LocalInspectionMode.current

    val firstAspectRatio = imageAspectRatios.firstOrNull() ?: ImageAspectRatio.SQUARE

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            contentPadding = contentPadding,
            pageSpacing = pageSpacing,
            modifier = Modifier.animateContentSize(),
        ) { page ->
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(firstAspectRatio.ratio)
                        .clip(RoundedCornerShape(ProductImageCornerRadius))
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

                if (page == 0) {
                    Text(
                        text = stringResource(R.string.feed_card_price_format, price),
                        modifier =
                            Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = PriceTextPadding, bottom = PriceTextPadding),
                        color = BuyOrNotTheme.colors.gray0,
                        style =
                            priceStyle.copy(
                                shadow =
                                    Shadow(
                                        color = Color.Black.copy(alpha = 0.3f),
                                        offset = Offset(0f, 4f),
                                        blurRadius = 4f,
                                    ),
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedVoteSection(
    hasVoted: Boolean,
    isVoteEnded: Boolean,
    isOwner: Boolean,
    userVotedOptionIndex: Int?,
    buyPercentage: Int,
    maybePercentage: Int,
    totalVoteCount: Int,
    voterProfileImageUrl: String,
    onVote: (Int) -> Unit,
    modifier: Modifier = Modifier,
    commentCount: Int? = null,
    onCommentClick: () -> Unit = {},
) {
    val isTie = buyPercentage == maybePercentage
    val hasVotes = totalVoteCount > 0

    Column(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (hasVoted || isVoteEnded || isOwner) {
                VoteProgressItem(
                    text = stringResource(R.string.feed_card_vote_buy),
                    percentage = buyPercentage / 100f,
                    percentageText = "$buyPercentage%",
                    progressBarColor = BuyOrNotTheme.colors.gray950,
                    shouldInvertTextColor = true,
                    textColor = if (isTie && !hasVotes) BuyOrNotTheme.colors.gray700 else BuyOrNotTheme.colors.gray800,
                    percentageTextColor = if (isTie && !hasVotes) BuyOrNotTheme.colors.gray700 else BuyOrNotTheme.colors.gray950,
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
                    progressBarColor = if (isTie && hasVotes) BuyOrNotTheme.colors.gray950 else BuyOrNotTheme.colors.gray400,
                    textColor = BuyOrNotTheme.colors.gray700,
                    percentageTextColor = if (isTie && hasVotes) BuyOrNotTheme.colors.gray950 else BuyOrNotTheme.colors.gray700,
                    shouldInvertTextColor = isTie && hasVotes,
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val statusText =
                if (isVoteEnded) {
                    stringResource(R.string.feed_card_vote_status_ended)
                } else {
                    stringResource(R.string.feed_card_vote_status_ongoing)
                }
            Row(
                modifier = Modifier.padding(start = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.feed_card_vote_count_format, totalVoteCount.toDisplayCount()),
                    style = BuyOrNotTheme.typography.bodyB5Medium,
                    color = BuyOrNotTheme.colors.gray600,
                )
                Text(
                    text = "∙",
                    style = BuyOrNotTheme.typography.bodyB7Medium,
                    color = BuyOrNotTheme.colors.gray600,
                )
                Text(
                    text = statusText,
                    style = BuyOrNotTheme.typography.bodyB5Medium,
                    color = BuyOrNotTheme.colors.gray600,
                )
            }
            if (commentCount != null) {
                CommentCount(
                    count = commentCount,
                    onClick = onCommentClick,
                    modifier = Modifier.padding(end = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun CommentCount(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.nonRippleClickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = BuyOrNotIcons.Comment.asImageVector(),
            contentDescription = "댓글",
            modifier = Modifier.size(13.dp),
            tint = BuyOrNotTheme.colors.gray600,
        )
        Text(
            text = count.toDisplayCount(),
            style = BuyOrNotTheme.typography.bodyB5Medium,
            color = BuyOrNotTheme.colors.gray600,
        )
    }
}

private const val MAX_DISPLAY_COUNT = 99

/** 투표 수·댓글 수는 상한을 넘으면 "99+"처럼 줄여 보여준다. */
private fun Int.toDisplayCount(): String = if (this > MAX_DISPLAY_COUNT) "$MAX_DISPLAY_COUNT+" else toString()

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
                    horizontal = 15.dp,
                    vertical = 14.dp,
                ),
            style = BuyOrNotTheme.typography.subTitleS4SemiBold,
            color = BuyOrNotTheme.colors.gray950,
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
                        color = BuyOrNotTheme.colors.gray1000.copy(alpha = 0.4f),
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
                ).nonRippleClickable(onClick = onDismiss)
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
    name = "FeedCard - Square (1x1) Interactive",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun FeedCardSquareInteractivePreview() {
    BuyOrNotTheme {
        var userVotedOption by remember { mutableStateOf<Int?>(null) }

        FeedCard(
            profileImageUrl = PreviewImages.avatar(),
            nickname = "결정장애",
            category = "뷰티",
            createdAt = "10분 전",
            title = "립스틱 살까요?",
            content = "이 립스틱 색상 어때요? 평소에 안 바르던 색인데 도전해볼까 고민중이에요!",
            productImageUrls =
                listOf(
                    PreviewImages.square(),
                    PreviewImages.square(),
                    PreviewImages.square(),
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
            productLink = "link",
            showProductLinkTooltip = true,
        )
    }
}

@Preview(
    name = "FeedCard - Landscape (5x4) Interactive",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun FeedCardLandscapeInteractivePreview() {
    BuyOrNotTheme {
        var userVotedOption by remember { mutableStateOf<Int?>(null) }

        FeedCard(
            profileImageUrl = PreviewImages.avatar(),
            nickname = "가로러버",
            category = "가전",
            createdAt = "1시간 전",
            title = "이 모니터 살까요?",
            content = "가로로 긴 제품은 5:4 비율로 보면 좋아요!",
            productImageUrls =
                listOf(
                    PreviewImages.landscape(),
                ),
            price = "299,000",
            imageAspectRatios = listOf(ImageAspectRatio.LANDSCAPE),
            isVoteEnded = false,
            userVotedOptionIndex = userVotedOption,
            buyVoteCount = 12,
            maybeVoteCount = 8,
            totalVoteCount = 20,
            onVote = { optionIndex ->
                userVotedOption = optionIndex
            },
            onDeleteClick = {},
            onReportClick = {},
        )
    }
}

@Preview(
    name = "FeedCard - Portrait (4x5) Interactive",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun FeedCardPortraitInteractivePreview() {
    BuyOrNotTheme {
        var userVotedOption by remember { mutableStateOf<Int?>(null) }

        FeedCard(
            profileImageUrl = PreviewImages.avatar(),
            nickname = "패션피플",
            category = "의류",
            createdAt = "2시간 전",
            title = "이 원피스 어때요?",
            content = "이 원피스 4:5 비율로 보면 더 예쁜 것 같아요! 세로로 긴 옷 사진은 이 비율이 딱이에요.",
            productImageUrls =
                listOf(
                    PreviewImages.portrait(),
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

@Preview(
    name = "FeedCard - Thread (댓글 있음)",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun FeedCardThreadWithCommentPreview() {
    BuyOrNotTheme {
        FeedCard(
            profileImageUrl = PreviewImages.avatar(),
            nickname = "참새방앗간12456",
            category = "패션 ∙ 잡화",
            createdAt = "6시간 전",
            title = "장화 살지말지 고민됩니다",
            content = "장마가 이미 끝나버리긴 했는데 지금 할인기간이라 매우 고민됩니다..",
            productImageUrls = listOf(PreviewImages.square()),
            price = "31,900",
            imageAspectRatios = listOf(ImageAspectRatio.SQUARE),
            isVoteEnded = false,
            buyVoteCount = 12,
            maybeVoteCount = 4,
            totalVoteCount = 16,
            onVote = {},
            useThreadLayout = true,
            commentCount = 120,
            latestComment =
                FeedCommentPreview(
                    nickname = "토봉이날다12456",
                    content = "이거 저 사봤는데 겁나 무겁고.. 그냥 그래요..",
                ),
        )
    }
}

@Preview(
    name = "FeedCard - Thread (댓글 없음)",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun FeedCardThreadWithoutCommentPreview() {
    BuyOrNotTheme {
        FeedCard(
            profileImageUrl = PreviewImages.avatar(),
            nickname = "참새방앗간12456",
            category = "패션 ∙ 잡화",
            createdAt = "6시간 전",
            title = "장화 살지말지 고민됩니다",
            content = "장마가 이미 끝나버리긴 했는데 지금 할인기간이라 매우 고민됩니다..",
            productImageUrls = listOf(PreviewImages.square()),
            price = "31,900",
            imageAspectRatios = listOf(ImageAspectRatio.SQUARE),
            isVoteEnded = false,
            userVotedOptionIndex = 0,
            buyVoteCount = 12,
            maybeVoteCount = 4,
            totalVoteCount = 16,
            onVote = {},
            useThreadLayout = true,
            commentCount = 0,
        )
    }
}
