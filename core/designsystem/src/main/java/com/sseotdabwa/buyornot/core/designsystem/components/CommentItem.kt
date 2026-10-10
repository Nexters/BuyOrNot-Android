package com.sseotdabwa.buyornot.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sseotdabwa.buyornot.core.designsystem.R
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.preview.PreviewImages
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

/** 댓글 작성자 아바타 좌측 상단에 겹쳐 그리는 투표 말풍선. */
enum class CommentVoteBubble(
    val emoji: String,
) {
    /** [사! 가즈아!]에 투표 */
    BUY("👍"),

    /** [애매하긴 해]에 투표 */
    UNSURE("🤔"),
}

enum class CommentTagStyle {
    /** 투표 선택지 태그 */
    DEFAULT,

    /** "작성자" 태그 */
    BRAND,
}

data class CommentTag(
    val text: String,
    val style: CommentTagStyle = CommentTagStyle.DEFAULT,
)

/**
 * 피드 상세의 댓글 한 줄.
 *
 * @param menuItems 더보기(⋯) 메뉴 항목. 비어 있으면 더보기 버튼을 숨긴다.
 */
@Composable
fun CommentItem(
    nickname: String,
    profileImageUrl: String?,
    createdAt: String,
    content: String,
    modifier: Modifier = Modifier,
    tag: CommentTag? = null,
    voteBubble: CommentVoteBubble? = null,
    menuItems: List<Pair<String, () -> Unit>> = emptyList(),
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box {
            CommentAvatar(profileImageUrl = profileImageUrl, size = 32)
            if (voteBubble != null) {
                VoteBubble(
                    bubble = voteBubble,
                    modifier = Modifier.offset(x = 14.dp, y = (-6).dp),
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = nickname,
                        style = BuyOrNotTheme.typography.titleT5Bold,
                        color = BuyOrNotTheme.colors.gray950,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "∙",
                        style = BuyOrNotTheme.typography.bodyB7Medium,
                        color = BuyOrNotTheme.colors.gray600,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = createdAt,
                        style = BuyOrNotTheme.typography.bodyB6Medium,
                        color = BuyOrNotTheme.colors.gray600,
                        maxLines = 1,
                    )
                    if (tag != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        CommentTagChip(tag = tag)
                    }
                }
                if (menuItems.isNotEmpty()) {
                    CommentMoreButton(menuItems = menuItems)
                }
            }
            Text(
                text = content,
                style = BuyOrNotTheme.typography.bodyB4Medium,
                color = BuyOrNotTheme.colors.gray900,
            )
        }
    }
}

@Composable
private fun CommentMoreButton(menuItems: List<Pair<String, () -> Unit>>) {
    var showMenu by remember { mutableStateOf(false) }
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
            ActionPopup(
                items =
                    menuItems.map { (label, onClick) ->
                        label to {
                            showMenu = false
                            onClick()
                        }
                    },
                onDismiss = { showMenu = false },
            )
        }
    }
}

@Composable
private fun CommentTagChip(tag: CommentTag) {
    val (background, textColor) =
        when (tag.style) {
            CommentTagStyle.DEFAULT -> BuyOrNotTheme.colors.gray200 to BuyOrNotTheme.colors.gray800
            CommentTagStyle.BRAND -> BuyOrNotTheme.colors.orange50 to BuyOrNotTheme.colors.orange100
        }
    Text(
        text = tag.text,
        modifier =
            Modifier
                .background(color = background, shape = RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp),
        style = BuyOrNotTheme.typography.bodyB7Medium,
        color = textColor,
        maxLines = 1,
    )
}

/**
 * 원형 프로필 이미지. 이미지가 없으면(비회원 등) 회색 원만 그린다.
 */
@Composable
fun CommentAvatar(
    profileImageUrl: String?,
    size: Int,
    modifier: Modifier = Modifier,
) {
    val avatarModifier =
        modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(BuyOrNotTheme.colors.gray100)
            .border(width = 1.dp, color = BuyOrNotTheme.colors.gray300, shape = CircleShape)
    if (LocalInspectionMode.current || profileImageUrl.isNullOrEmpty()) {
        Box(modifier = avatarModifier)
    } else {
        AsyncImage(
            model = profileImageUrl,
            contentDescription = null,
            modifier = avatarModifier,
            contentScale = ContentScale.Crop,
        )
    }
}

/**
 * 몸통 아래에 꼬리가 달린 말풍선의 시안 좌표(dp). 꼬리는 왼쪽 변이 수직이고 끝이 왼쪽 아래에서 둥글게 맺힌다.
 * Shape는 [width] 기준 비율로 그려 실제 크기에 맞춘다.
 */
private class TailBubbleSpec(
    val width: Float,
    val bodyHeight: Float,
    val height: Float,
    val cornerRadius: Float,
    val tailStartX: Float,
    val tailEndX: Float,
) {
    val shape =
        GenericShape { size, _ ->
            val unit = size.width / width
            // 꼬리 끝 곡선은 꼬리 폭에 비례한다.
            val tail = (tailEndX - tailStartX) / BASE_TAIL_WIDTH * unit
            val bodyBottom = bodyHeight * unit
            val bottom = height * unit
            val startX = tailStartX * unit
            addRoundRect(
                RoundRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = bodyBottom,
                    cornerRadius = CornerRadius(cornerRadius * unit),
                ),
            )
            // 몸통과 겹치게 조금 위에서 시작해 이음새가 보이지 않게 한다.
            moveTo(startX, bodyBottom - tail)
            lineTo(tailEndX * unit, bodyBottom - tail)
            lineTo(startX + tail, bottom - tail * 0.4f)
            quadraticTo(startX, bottom + tail * 0.6f, startX, bottom - tail * 0.9f)
            close()
        }
}

private const val BASE_TAIL_WIDTH = 6f

private val AvatarBubble =
    TailBubbleSpec(width = 24f, bodyHeight = 22f, height = 25.5f, cornerRadius = 6.31f, tailStartX = 12.4f, tailEndX = 18.4f)

private val LockBubble =
    TailBubbleSpec(width = 72f, bodyHeight = 55f, height = 63f, cornerRadius = 15.43f, tailStartX = 37f, tailEndX = 54f)

/** 말풍선은 dp 고정이라 이모지도 글꼴 배율을 따르지 않게 한다. */
@Composable
private fun FixedScaleEmoji(
    emoji: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 1f)) {
        Text(text = emoji, style = style, modifier = modifier)
    }
}

@Composable
private fun VoteBubble(
    bubble: CommentVoteBubble,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(width = AvatarBubble.width.dp, height = AvatarBubble.height.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = AvatarBubble.shape,
                    ambientColor = Color(0xCCD2D3D9),
                    spotColor = Color(0xCCD2D3D9),
                ).background(BuyOrNotTheme.colors.gray0, AvatarBubble.shape),
        contentAlignment = Alignment.TopCenter,
    ) {
        FixedScaleEmoji(
            emoji = bubble.emoji,
            style = BuyOrNotTheme.typography.bodyB4Medium,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

// 하드웨어 그림자는 색 알파에 시스템 그림자 알파(약 0.2)를 곱하므로, 시안의 20%를 내려면 불투명하게 준다.
private val LockBubbleShadow = Color(0xFF696E77)
private val LockEmojiSize = 35.dp

/**
 * 투표 전 댓글 영역을 가리는 안내. 두 선택지 말풍선이 마주 보며 기울어져 있다.
 */
@Composable
fun CommentVoteLock(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(width = 170.dp, height = 100.dp)) {
            VoteLockBubble(
                emoji = CommentVoteBubble.BUY.emoji,
                mirrored = true,
                rotation = -7.59f,
                modifier = Modifier.offset(x = 6.dp, y = 27.dp),
            )
            VoteLockBubble(
                emoji = CommentVoteBubble.UNSURE.emoji,
                mirrored = false,
                rotation = 6.87f,
                modifier = Modifier.offset(x = 91.dp, y = 11.dp),
            )
        }
        Spacer(modifier = Modifier.height(21.dp))
        Text(
            text = "투표 후 댓글을 볼 수 있어요!",
            style = BuyOrNotTheme.typography.subTitleS3SemiBold,
            color = BuyOrNotTheme.colors.gray950,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "당신의 선택은?",
            style = BuyOrNotTheme.typography.subTitleS3SemiBold,
            color = BuyOrNotTheme.colors.gray1000,
        )
    }
}

@Composable
private fun VoteLockBubble(
    emoji: String,
    mirrored: Boolean,
    rotation: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(width = LockBubble.width.dp, height = LockBubble.height.dp)
                .graphicsLayer { rotationZ = rotation },
        contentAlignment = Alignment.TopCenter,
    ) {
        // 꼬리 방향만 뒤집고 이모지는 뒤집지 않는다.
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .graphicsLayer { scaleX = if (mirrored) -1f else 1f }
                    .shadow(
                        elevation = 16.dp,
                        shape = LockBubble.shape,
                        ambientColor = LockBubbleShadow,
                        spotColor = LockBubbleShadow,
                    ).background(BuyOrNotTheme.colors.gray0, LockBubble.shape),
        )
        FixedScaleEmoji(
            emoji = emoji,
            style = BuyOrNotTheme.typography.bodyB4Medium.copy(fontSize = 30.sp, lineHeight = LockEmojiSize.value.sp),
            modifier = Modifier.padding(top = ((LockBubble.bodyHeight.dp - LockEmojiSize) / 2)),
        )
    }
}

/**
 * 댓글이 하나도 없을 때의 안내.
 *
 * @param onWriteClick 댓글을 쓸 수 있을 때만 넘긴다. null이면 "댓글 쓰기" 버튼을 숨긴다.
 */
@Composable
fun CommentEmpty(
    onWriteClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_comment_empty),
            contentDescription = null,
            modifier = Modifier.size(width = 84.dp, height = 68.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "첫번째 댓글을 남겨보세요!",
            style = BuyOrNotTheme.typography.titleT3Bold,
            color = BuyOrNotTheme.colors.gray800,
        )
        if (onWriteClick != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "댓글 쓰기",
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(BuyOrNotTheme.colors.gray100)
                        .clickable(onClick = onWriteClick)
                        .padding(12.dp),
                style = BuyOrNotTheme.typography.subTitleS5SemiBold,
                color = BuyOrNotTheme.colors.gray700,
            )
        }
    }
}

/**
 * 홈 피드 카드 하단의 최신 댓글 미리보기.
 */
@Composable
fun CommentPreviewCard(
    profileImageUrl: String?,
    content: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(BuyOrNotTheme.colors.gray100, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CommentAvatar(profileImageUrl = profileImageUrl, size = 24)
        Text(
            text = content,
            style = BuyOrNotTheme.typography.bodyB5Medium,
            color = BuyOrNotTheme.colors.gray900,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(name = "CommentItem - 케이스", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CommentItemPreview() {
    BuyOrNotTheme {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            CommentItem(
                nickname = "거북이날다12456",
                profileImageUrl = PreviewImages.avatar(),
                createdAt = "2시간 전",
                content = "이거 저 사봤는데 겁나 무겁고.. 그냥 그래요..",
                tag = CommentTag("사! 가즈아!"),
                voteBubble = CommentVoteBubble.BUY,
                menuItems = listOf("신고하기" to {}),
            )
            CommentItem(
                nickname = "토봉이날다12456",
                profileImageUrl = PreviewImages.avatar(),
                createdAt = "2시간 전",
                content = "이거 저 사봤는데 겁나 무겁고.. 그냥 그래요..",
                tag = CommentTag("애매하긴 해"),
                voteBubble = CommentVoteBubble.UNSURE,
                menuItems = listOf("삭제하기" to {}),
            )
            CommentItem(
                nickname = "토봉이날다12456",
                profileImageUrl = PreviewImages.avatar(),
                createdAt = "2시간 전",
                content = "이거 저 사봤는데 겁나 무겁고.. 그냥 그래요..",
                tag = CommentTag("작성자", CommentTagStyle.BRAND),
                menuItems = listOf("신고하기" to {}),
            )
            CommentItem(
                nickname = "지름신들린수달_1234",
                profileImageUrl = null,
                createdAt = "2시간 전",
                content = "이거 저 사봤는데 겁나 무겁고.. 그냥 그래요..",
                menuItems = listOf("신고하기" to {}),
            )
        }
    }
}

@Preview(name = "CommentPreviewCard", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CommentPreviewCardPreview() {
    BuyOrNotTheme {
        CommentPreviewCard(
            profileImageUrl = null,
            content = "이거 저 사봤는데 겁나 무겁고.. 그냥 그래요..",
            modifier = Modifier.padding(20.dp),
        )
    }
}

@Preview(name = "CommentVoteLock", showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 375)
@Composable
private fun CommentVoteLockPreview() {
    BuyOrNotTheme {
        CommentVoteLock(modifier = Modifier.padding(vertical = 40.dp))
    }
}

@Preview(name = "CommentEmpty", showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 375)
@Composable
private fun CommentEmptyPreview() {
    BuyOrNotTheme {
        CommentEmpty(onWriteClick = {}, modifier = Modifier.padding(vertical = 40.dp))
    }
}
