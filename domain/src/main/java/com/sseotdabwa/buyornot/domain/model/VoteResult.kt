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
    /** 투표 완료 스낵바에 쓰는 피드 대표(첫 번째) 이미지. */
    val feedImageUrl: String? = null,
)
