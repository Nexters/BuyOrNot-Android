package com.sseotdabwa.buyornot.core.data.repository

import com.sseotdabwa.buyornot.core.network.dto.response.LatestCommentDto
import com.sseotdabwa.buyornot.domain.model.CommentAuthorType
import com.sseotdabwa.buyornot.domain.model.CommentPreview
import com.sseotdabwa.buyornot.domain.model.VoteChoice

internal fun String.toVoteChoice(): VoteChoice? =
    when (this) {
        "YES" -> VoteChoice.YES
        "NO" -> VoteChoice.NO
        else -> null
    }

// 알 수 없는 값은 회원으로 본다 — 비회원으로 잘못 보이면 "비회원" 태그가 붙는다.
internal fun String.toCommentAuthorType(): CommentAuthorType = if (this == "GUEST") CommentAuthorType.GUEST else CommentAuthorType.MEMBER

internal fun LatestCommentDto.toDomain(): CommentPreview =
    CommentPreview(
        authorType = authorType.toCommentAuthorType(),
        nickname = nickname,
        profileImage = profileImage,
        content = content,
        isAuthor = isAuthor,
        voteChoice = voteChoice?.toVoteChoice(),
    )
