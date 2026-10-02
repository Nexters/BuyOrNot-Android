package com.sseotdabwa.buyornot.core.designsystem.components

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
            .background(BuyOrNotTheme.colors.gray200)
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

/** 오른쪽 아래로 꼬리가 달린 흰 말풍선 (24×22, 꼬리 6). */
private val VoteBubbleShape =
    GenericShape { size, _ ->
        val tail = 6.dp.value * (size.width / 24.dp.value)
        val bodyHeight = size.height - tail
        addRoundRect(
            RoundRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = bodyHeight,
                cornerRadius = CornerRadius(bodyHeight * 0.29f),
            ),
        )
        // 꼬리: 몸통 오른쪽 아래 모서리 안쪽에서 아래로 뾰족하게
        moveTo(size.width - tail * 2.2f, bodyHeight - 1f)
        lineTo(size.width - tail * 0.8f, bodyHeight - 1f)
        lineTo(size.width - tail * 0.9f, size.height)
        close()
    }

@Composable
private fun VoteBubble(
    bubble: CommentVoteBubble,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(width = 24.dp, height = 26.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = VoteBubbleShape,
                    ambientColor = Color(0xCCD2D3D9),
                    spotColor = Color(0xCCD2D3D9),
                ).background(BuyOrNotTheme.colors.gray0, VoteBubbleShape),
        contentAlignment = Alignment.TopCenter,
    ) {
        Text(
            text = bubble.emoji,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}

/**
 * 홈 피드 카드 하단의 최신 댓글 미리보기.
 */
@Composable
fun CommentPreviewCard(
    nickname: String,
    content: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(BuyOrNotTheme.colors.gray100, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            text = nickname,
            style = BuyOrNotTheme.typography.titleT6Bold,
            color = BuyOrNotTheme.colors.gray950,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(6.dp))
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
            nickname = "토봉이날다12456",
            content = "이거 저 사봤는데 겁나 무겁고.. 그냥 그래요..",
            modifier = Modifier.padding(20.dp),
        )
    }
}
