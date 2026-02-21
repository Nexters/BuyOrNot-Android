package com.sseotdabwa.buyornot.domain.repository

import com.sseotdabwa.buyornot.domain.model.Feed
import com.sseotdabwa.buyornot.domain.model.FeedCategory
import com.sseotdabwa.buyornot.domain.model.UploadInfo
import com.sseotdabwa.buyornot.domain.model.VoteChoice
import com.sseotdabwa.buyornot.domain.model.VoteResult

interface FeedRepository {
    /**
     * 전체 피드 목록 조회
     *
     * @param cursor 이전 페이지 마지막 feedId (첫 페이지는 생략)
     * @param size 페이지 크기
     * @param feedStatus 피드 상태 필터 (OPEN, CLOSED / null이면 전체)
     * @return 피드 목록
     */
    suspend fun getFeedList(
        cursor: Long? = null,
        size: Int = 20,
        feedStatus: String? = null,
    ): List<Feed>

    /**
     * 내가 작성한 피드 목록 조회
     *
     * @return 내가 작성한 피드 목록
     */
    suspend fun getMyFeeds(): List<Feed>

    suspend fun getPresignedUrl(
        fileName: String,
        contentType: String,
    ): UploadInfo

    suspend fun uploadImage(
        url: String,
        bytes: ByteArray,
        contentType: String,
    )

    suspend fun createFeed(
        category: FeedCategory,
        price: Int,
        content: String,
        s3ObjectKey: String,
        imageWidth: Int,
        imageHeight: Int,
    ): Long

    /**
     * 피드 삭제
     *
     * @param feedId 삭제할 피드 ID
     */
    suspend fun deleteFeed(feedId: Long)

    /**
     * 피드 신고
     *
     * @param feedId 신고할 피드 ID
     */
    suspend fun reportFeed(feedId: Long)

    /**
     * 회원 투표
     *
     * @param feedId 투표할 피드 ID
     * @param choice 투표 선택 (YES or NO)
     * @return 투표 결과
     */
    suspend fun voteFeed(
        feedId: Long,
        choice: VoteChoice,
    ): VoteResult

    /**
     * 비회원 투표
     *
     * @param feedId 투표할 피드 ID
     * @param choice 투표 선택 (YES or NO)
     * @return 투표 결과
     */
    suspend fun voteGuestFeed(
        feedId: Long,
        choice: VoteChoice,
    ): VoteResult
}
