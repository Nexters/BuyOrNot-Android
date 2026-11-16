package com.sseotdabwa.buyornot.domain.model

/**
 * 사진을 분석해 만든 투표 글 초안. 저장되지 않으며 사용자가 고친 뒤 피드 등록에 쓴다.
 *
 * @property title 생성에 실패했거나 금칙어가 들어가면 null.
 * @property content 생성에 실패했거나 금칙어가 들어가면 null.
 * @property price 사진에 원화 가격이 글자로 보일 때만 그 금액. 잘못 읽을 수 있어 사용자가 확인해야 한다.
 */
data class FeedAiDraft(
    val title: String?,
    val content: String?,
    val category: FeedCategory?,
    val price: Long?,
)
