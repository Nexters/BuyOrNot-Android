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
 * 피드 댓글(원댓글).
 *
 * @property isMine 조회하는 사람 기준 — 같은 댓글도 보는 사람마다 값이 다르다. 삭제/신고 노출 판단용.
 * @property isAuthor 작성자가 피드 원작성자인지 — 누가 조회하든 같다. "작성자" 태그용.
 * @property voteChoice 작성자의 투표 선택. 피드 작성자·미투표 회원·비회원은 항상 null.
 * @property isDeleted 답글이 남은 채 삭제된 원댓글. 작성자 정보와 내용은 비어 있고 공감할 수 없다.
 * @property replies 등록순 답글 전체. 답글 페이지의 고정 원댓글([CommentThread.comment])에서는 비어 있다.
 */
data class Comment(
    val id: Long,
    val authorType: CommentAuthorType,
    val nickname: String,
    val profileImage: ImageSource?,
    val content: String,
    val isMine: Boolean,
    val isAuthor: Boolean,
    val voteChoice: VoteChoice?,
    val createdAt: String,
    val isDeleted: Boolean = false,
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val replyCount: Int = 0,
    val replies: List<CommentReply> = emptyList(),
)

/**
 * 원댓글에 단 답글. 답글에는 답글·공감을 달 수 없고, 작성자의 투표 선택도 노출하지 않는다.
 *
 * @property isMine 조회하는 사람 기준. 삭제/신고 노출 판단용.
 * @property isAuthor 작성자가 피드 원작성자인지. "작성자" 태그용.
 */
data class CommentReply(
    val id: Long,
    val parentId: Long,
    val authorType: CommentAuthorType,
    val nickname: String,
    val profileImage: ImageSource?,
    val content: String,
    val isMine: Boolean,
    val isAuthor: Boolean,
    val createdAt: String,
)

/**
 * 답글 페이지 상단에 고정하는 원댓글.
 *
 * @property totalCount 헤더 "댓글 (n)"의 n. 원댓글(삭제됐으면 제외)과 답글 수의 합.
 */
data class CommentThread(
    val comment: Comment,
    val totalCount: Int,
)

data class CommentReplyPage(
    val replies: List<CommentReply>,
    val nextCursor: Long?,
    val hasNext: Boolean,
)

/** 공감·공감 취소 직후 서버가 돌려주는 최신 공감 상태. */
data class CommentLike(
    val likeCount: Int,
    val isLiked: Boolean,
)

/** 피드 카드에 노출하는 가장 최근 댓글 1개 미리보기. */
data class CommentPreview(
    val authorType: CommentAuthorType,
    val nickname: String,
    val profileImage: ImageSource?,
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
    const val NOT_FOUND = "COMMENT_004"
    const val SELF_REPORT = "COMMENT_008"
    const val ALREADY_REPORTED = "COMMENT_009"
    const val RATE_LIMITED = "COMMENT_010"
    const val PROFANITY = "COMMENT_011"
    const val PROFANITY_BLOCKED = "COMMENT_012"
    const val INVALID_PARENT = "COMMENT_014"
    const val LIKE_UNAVAILABLE = "COMMENT_016"
}

/** 댓글 본문 최대 길이 (공백 제거 후 1~300자). */
const val COMMENT_MAX_LENGTH = 300
