package com.sseotdabwa.buyornot.domain.model

/**
 * 투표 결과 도메인 모델
 */
data class VoteResult(
    val feedId: Long,
    val choice: VoteChoice,
    val yesCount: Int,
    val noCount: Int,
    val totalCount: Int,
)
