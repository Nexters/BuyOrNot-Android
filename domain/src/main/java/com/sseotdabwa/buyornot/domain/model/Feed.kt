package com.sseotdabwa.buyornot.domain.model

/**
 * 피드 이미지 도메인 모델
 */
data class FeedImage(
    val s3ObjectKey: String,
    val imageUrl: String,
    val imageWidth: Int,
    val imageHeight: Int,
)

/**
 * 피드 도메인 모델
 */
data class Feed(
    val feedId: Long,
    val title: String,
    val content: String,
    val price: String,
    val category: FeedCategory,
    val yesCount: Int,
    val noCount: Int,
    val totalCount: Int,
    val feedStatus: FeedStatus,
    val images: List<FeedImage>,
    val author: Author,
    val createdAt: String,
    val hasVoted: Boolean,
    val myVoteChoice: VoteChoice?,
    val productLink: String? = null,
) {
    val viewUrls: List<String> get() = images.map { it.imageUrl }
}

/**
 * 작성자 정보 도메인 모델
 */
data class Author(
    val userId: Long,
    val nickname: String,
    val profileImage: String?,
) {
    /**
     * 비회원이 작성한 글인가.
     *
     * 서버는 작성자가 없는 글(`Feed.user == null`)의 작성자를 `userId = 0`으로 내려준다
     * (`FeedResponseV2.buildAuthorResponse`). 차단 API는 실제 유저를 대상으로 하므로
     * 이 글은 차단할 수 없다. 신고는 유저가 아니라 피드를 대상으로 해서 그대로 가능하다.
     */
    val isGuest: Boolean get() = userId == GUEST_AUTHOR_USER_ID
}

/** 서버가 비회원 작성 글의 작성자 id로 쓰는 값. */
const val GUEST_AUTHOR_USER_ID = 0L

/**
 * 투표 선택
 */
enum class VoteChoice {
    YES,
    NO,
}

/**
 * 피드 상태
 */
enum class FeedStatus {
    OPEN,
    CLOSED,
}
