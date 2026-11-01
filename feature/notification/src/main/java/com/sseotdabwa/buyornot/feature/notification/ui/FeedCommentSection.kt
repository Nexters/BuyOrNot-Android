package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.common.util.TimeUtils
import com.sseotdabwa.buyornot.core.designsystem.components.ActionPopup
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotDivider
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotDividerSize
import com.sseotdabwa.buyornot.core.designsystem.components.CommentEmpty
import com.sseotdabwa.buyornot.core.designsystem.components.CommentItem
import com.sseotdabwa.buyornot.core.designsystem.components.CommentItemSize
import com.sseotdabwa.buyornot.core.designsystem.components.CommentLikeButton
import com.sseotdabwa.buyornot.core.designsystem.components.CommentReplyButton
import com.sseotdabwa.buyornot.core.designsystem.components.CommentTag
import com.sseotdabwa.buyornot.core.designsystem.components.CommentTagStyle
import com.sseotdabwa.buyornot.core.designsystem.components.CommentThread
import com.sseotdabwa.buyornot.core.designsystem.components.CommentVoteBubble
import com.sseotdabwa.buyornot.core.designsystem.components.DeletedCommentItem
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.core.designsystem.util.nonRippleClickable
import com.sseotdabwa.buyornot.domain.model.Comment
import com.sseotdabwa.buyornot.domain.model.CommentReply
import com.sseotdabwa.buyornot.domain.model.CommentSort
import com.sseotdabwa.buyornot.domain.model.VoteChoice

private const val COMMENT_HEADER_KEY = "comment_header"

/** 댓글 한 건의 위아래 여백. 목록 끝 여백 계산에도 쓴다. */
internal val CommentItemVerticalPadding = 20.dp

/** 투표 선택지 문구. 태그와 입력창 안내에 쓴다. */
internal fun VoteChoice.label(): String =
    when (this) {
        VoteChoice.YES -> "사! 가즈아!"
        VoteChoice.NO -> "애매하긴 해"
    }

private val CommentSort.label: String
    get() =
        when (this) {
            CommentSort.REGISTERED -> "등록순"
            CommentSort.LATEST -> "최신순"
        }

/** 피드 상세의 댓글 영역: 정렬 헤더 + 댓글 목록 + 다음 페이지 로딩. */
internal fun LazyListScope.commentSection(
    uiState: NotificationDetailUiState,
    onIntent: (NotificationDetailIntent) -> Unit,
    onWriteCommentClick: () -> Unit,
) {
    item(key = COMMENT_HEADER_KEY) {
        CommentHeader(
            sort = uiState.commentSort,
            showSort = uiState.canViewComments,
            onSortSelected = { onIntent(NotificationDetailIntent.OnCommentSortSelected(it)) },
        )
    }

    when {
        uiState.isCommentsLoading && uiState.comments.isEmpty() -> item(key = "comment_loading") { CommentLoading() }
        uiState.comments.isEmpty() ->
            item(key = "comment_empty") {
                CommentEmpty(
                    onWriteClick = onWriteCommentClick.takeIf { uiState.canWriteComment },
                    modifier = Modifier.padding(top = 50.dp, bottom = 20.dp),
                )
            }
        else -> {
            itemsIndexed(items = uiState.comments, key = { _, comment -> comment.id }) { index, comment ->
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    FeedCommentThread(
                        comment = comment,
                        isGuestViewer = uiState.isGuest,
                        onReplyClick = { onIntent(NotificationDetailIntent.OnReplyClicked(comment.id)) }.takeIf { uiState.canWriteComment },
                        onLikeClick = { onIntent(NotificationDetailIntent.OnLikeClicked(comment.id)) }.takeIf { uiState.canWriteComment },
                        onDelete = { onIntent(NotificationDetailIntent.ShowDeleteCommentDialog(it)) },
                        onReport = { onIntent(NotificationDetailIntent.ShowReportCommentDialog(it)) },
                        modifier = Modifier.padding(vertical = CommentItemVerticalPadding),
                    )
                    // 댓글과 댓글 사이에만 두고 마지막 댓글 아래에는 없다.
                    if (index < uiState.comments.lastIndex) {
                        BuyOrNotDivider(size = BuyOrNotDividerSize.Small)
                    }
                }
            }
            if (uiState.isNextCommentsLoading) {
                item(key = "comment_next_loading") { CommentLoading() }
            }
        }
    }
}

/**
 * 원댓글과 그 아래 답글 스레드.
 *
 * @param onReplyClick null이면 [답글쓰기]를 숨긴다 (비회원·답글 페이지의 고정 원댓글).
 * @param onLikeClick null이면 [공감]을 숨긴다 (비회원).
 */
@Composable
internal fun FeedCommentThread(
    comment: Comment,
    isGuestViewer: Boolean,
    onReplyClick: (() -> Unit)?,
    onLikeClick: (() -> Unit)?,
    onDelete: (CommentTarget) -> Unit,
    onReport: (CommentTarget) -> Unit,
    modifier: Modifier = Modifier,
    replies: List<CommentReply> = comment.replies,
) {
    CommentThread(
        parent = {
            if (comment.isDeleted) {
                DeletedCommentItem()
            } else {
                CommentItem(
                    nickname = comment.nickname,
                    profileImageUrl = comment.profileImage,
                    createdAt = TimeUtils.formatRelativeTime(comment.createdAt),
                    content = comment.content,
                    tag = comment.tag(),
                    voteBubble = comment.voteBubble(),
                    menuItems = menuItems(isMine = comment.isMine, isGuestViewer, CommentTarget(comment.id), onDelete, onReport),
                    actions =
                        if (onReplyClick == null && onLikeClick == null) {
                            null
                        } else {
                            {
                                if (onReplyClick != null) CommentReplyButton(replyCount = comment.replyCount, onClick = onReplyClick)
                                if (onLikeClick != null) {
                                    CommentLikeButton(likeCount = comment.likeCount, isLiked = comment.isLiked, onClick = onLikeClick)
                                }
                            }
                        },
                )
            }
        },
        replies = replies,
        replyKey = { it.id },
        reply = { reply ->
            CommentItem(
                nickname = reply.nickname,
                profileImageUrl = reply.profileImage,
                createdAt = TimeUtils.formatRelativeTime(reply.createdAt),
                content = reply.content,
                size = CommentItemSize.REPLY,
                tag = reply.tag(),
                menuItems = menuItems(reply.isMine, isGuestViewer, CommentTarget(comment.id, reply.id), onDelete, onReport),
            )
        },
        modifier = modifier,
    )
}

// 투표한 사람을 부각하려고 회원/비회원은 구분하지 않고 작성자·투표자에게만 태그를 단다.
private fun Comment.tag(): CommentTag? =
    when {
        isAuthor -> CommentTag("작성자", CommentTagStyle.BRAND)
        else -> voteChoice?.let { CommentTag(it.label()) }
    }

// 피드 작성자는 투표하지 않으므로 이모지를 붙이지 않는다.
private fun Comment.voteBubble(): CommentVoteBubble? =
    when {
        isAuthor -> null
        voteChoice == VoteChoice.YES -> CommentVoteBubble.BUY
        voteChoice == VoteChoice.NO -> CommentVoteBubble.UNSURE
        else -> null
    }

// 투표 내용은 투표 페이지에서 남긴 원댓글에만 붙이므로 답글에는 작성자 태그만 단다.
private fun CommentReply.tag(): CommentTag? = if (isAuthor) CommentTag("작성자", CommentTagStyle.BRAND) else null

// 내 댓글은 삭제만, 남의 댓글은 신고만 — 본인 댓글 신고는 서버가 거절한다(COMMENT_008).
private fun menuItems(
    isMine: Boolean,
    isGuestViewer: Boolean,
    target: CommentTarget,
    onDelete: (CommentTarget) -> Unit,
    onReport: (CommentTarget) -> Unit,
): List<Pair<String, () -> Unit>> =
    when {
        isGuestViewer -> emptyList()
        isMine -> listOf("삭제하기" to { onDelete(target) })
        else -> listOf("신고하기" to { onReport(target) })
    }

@Composable
private fun CommentHeader(
    sort: CommentSort,
    showSort: Boolean,
    onSortSelected: (CommentSort) -> Unit,
) {
    Column {
        BuyOrNotDivider(size = BuyOrNotDividerSize.Small)
        if (showSort) {
            var showMenu by remember { mutableStateOf(false) }
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Box {
                    Row(
                        modifier = Modifier.nonRippleClickable { showMenu = true },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = sort.label,
                            style = BuyOrNotTheme.typography.bodyB4Medium,
                            color = BuyOrNotTheme.colors.gray800,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = BuyOrNotIcons.ArrowDown.asImageVector(),
                            contentDescription = "정렬 변경",
                            modifier = Modifier.size(12.dp),
                            tint = BuyOrNotTheme.colors.gray800,
                        )
                    }
                    if (showMenu) {
                        ActionPopup(
                            selectedIndex = CommentSort.entries.indexOf(sort),
                            items =
                                CommentSort.entries.map { option ->
                                    option.label to {
                                        showMenu = false
                                        onSortSelected(option)
                                    }
                                },
                            onDismiss = { showMenu = false },
                        )
                    }
                }
            }
            BuyOrNotDivider(size = BuyOrNotDividerSize.Small)
        }
    }
}

@Composable
internal fun CommentLoading() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(80.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = BuyOrNotTheme.colors.gray950,
            strokeWidth = 2.dp,
        )
    }
}
