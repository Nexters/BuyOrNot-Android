package com.sseotdabwa.buyornot.domain.repository

import com.sseotdabwa.buyornot.domain.model.FeedCategory
import com.sseotdabwa.buyornot.domain.model.PostDraft

interface PostDraftRepository {
    /**
     * 상품 사진으로 투표 글 제목·내용 초안을 생성한다.
     *
     * @throws com.sseotdabwa.buyornot.domain.exception.PostDraftLimitExceededException 하루 생성 한도를 넘은 경우
     */
    suspend fun generateDraft(
        image: ByteArray,
        category: FeedCategory?,
        price: Int?,
    ): PostDraft
}
