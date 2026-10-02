package com.sseotdabwa.buyornot.domain.model

enum class CommentAuthorType {
    MEMBER,
    GUEST,
}

enum class CommentSort {
    /** 등록순 (서버 기본값) */
    REGISTERED,

    /** 최신순 */
    LATEST,
}

/**
 * 피드 댓글.
 *
 * @property isMine 조회하는 사람 기준 — 같은 댓글도 보는 사람마다 값이 다르다. 삭제/신고 노출 판단용.
 * @property isAuthor 작성자가 피드 원작성자인지 — 누가 조회하든 같다. "작성자" 태그용.
 * @property voteChoice 작성자의 투표 선택. 피드 작성자·미투표 회원·비회원은 항상 null.
 */
data class Comment(
    val id: Long,
    val authorType: CommentAuthorType,
    val nickname: String,
    val profileImage: String?,
    val content: String,
    val isMine: Boolean,
    val isAuthor: Boolean,
    val voteChoice: VoteChoice?,
    val createdAt: String,
)

/** 피드 카드에 노출하는 가장 최근 댓글 1개 미리보기. */
data class CommentPreview(
    val authorType: CommentAuthorType,
    val nickname: String,
    val profileImage: String?,
    val content: String,
    val isAuthor: Boolean,
    val voteChoice: VoteChoice?,
)

data class CommentPage(
    val comments: List<Comment>,
    val nextCursor: Long?,
    val hasNext: Boolean,
)

/** 서버가 댓글 요청을 거절할 때 내려주는 에러 코드. */
object CommentErrorCode {
    const val CLOSED_FEED = "COMMENT_003"
    const val NOT_FOUND = "COMMENT_004"
    const val SELF_REPORT = "COMMENT_008"
    const val ALREADY_REPORTED = "COMMENT_009"
    const val RATE_LIMITED = "COMMENT_010"
    const val PROFANITY = "COMMENT_011"
    const val PROFANITY_BLOCKED = "COMMENT_012"
}

/** 댓글 본문 최대 길이 (공백 제거 후 1~300자). */
const val COMMENT_MAX_LENGTH = 300
