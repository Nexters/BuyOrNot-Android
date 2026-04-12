package com.sseotdabwa.buyornot.domain.model

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
    val s3ObjectKey: String,
    val viewUrls: List<String>,
    val imageWidth: Int,
    val imageHeight: Int,
    val author: Author,
    val createdAt: String,
    val hasVoted: Boolean,
    val myVoteChoice: VoteChoice?,
)

/**
 * 작성자 정보 도메인 모델
 */
data class Author(
    val userId: Long,
    val nickname: String,
    val profileImage: String?,
)

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
