package com.sseotdabwa.buyornot.core.network.api

import com.sseotdabwa.buyornot.core.network.dto.request.FeedRequest
import com.sseotdabwa.buyornot.core.network.dto.request.PresignedUrlRequest
import com.sseotdabwa.buyornot.core.network.dto.request.VoteRequest
import com.sseotdabwa.buyornot.core.network.dto.response.BaseResponse
import com.sseotdabwa.buyornot.core.network.dto.response.FeedItemDto
import com.sseotdabwa.buyornot.core.network.dto.response.FeedListResponse
import com.sseotdabwa.buyornot.core.network.dto.response.FeedResponse
import com.sseotdabwa.buyornot.core.network.dto.response.PresignedUrlResponse
import com.sseotdabwa.buyornot.core.network.dto.response.VoteResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface FeedApiService {
    /**
     * 전체 피드 목록 조회 (페이징)
     *
     * @param cursor 이전 페이지 마지막 feedId (첫 페이지는 생략)
     * @param size 페이지 크기 (기본값 20, 최대 50)
     * @param feedStatus 피드 상태 필터 (OPEN, CLOSED / 미지정 시 전체)
     */
    @GET("/api/v2/feeds")
    suspend fun getFeedList(
        @Query("cursor") cursor: Long? = null,
        @Query("size") size: Int = 20,
        @Query("feedStatus") feedStatus: String? = null,
        @Query("category") category: List<String>? = null,
    ): BaseResponse<FeedListResponse>

    /**
     * 피드 단건 조회
     *
     * @param feedId 조회할 피드 ID
     */
    @GET("/api/v2/feeds/{feedId}")
    suspend fun getFeed(
        @Path("feedId") feedId: Long,
    ): BaseResponse<FeedItemDto>

    /**
     * 내가 작성한 피드 목록 조회 (페이지네이션)
     *
     * @param cursor 이전 페이지 마지막 feedId (첫 페이지는 생략)
     * @param size 페이지 크기 (기본값 20, 최대 50)
     * @param feedStatus 피드 상태 필터 (OPEN, CLOSED / 미지정 시 전체)
     */
    @GET("/api/v1/users/me/feeds")
    suspend fun getMyFeeds(
        @Query("cursor") cursor: Long? = null,
        @Query("size") size: Int = 20,
        @Query("feedStatus") feedStatus: String? = null,
    ): BaseResponse<FeedListResponse>

    @POST("/api/v1/uploads/presigned-put")
    suspend fun getPresignedUrl(
        @Body request: PresignedUrlRequest,
    ): BaseResponse<PresignedUrlResponse>

    /**
     * S3 Pre-signed URL을 통한 이미지 바이너리 업로드
     * No-Authentication 헤더를 통해 인터셉터에서 토큰 삽입을 제외함
     */
    @PUT
    @Headers("No-Authentication: true")
    suspend fun uploadImage(
        @Url url: String,
        @Header("Content-Type") contentType: String,
        @Body body: RequestBody,
    ): Response<Unit>

    @POST("/api/v2/feeds")
    suspend fun createFeed(
        @Body request: FeedRequest,
    ): BaseResponse<FeedResponse>

    /**
     * 피드 삭제
     *
     * @param feedId 삭제할 피드 ID
     */
    @DELETE("/api/v1/feeds/{feedId}")
    suspend fun deleteFeed(
        @Path("feedId") feedId: Long,
    ): BaseResponse<Unit>

    /**
     * 피드 신고
     *
     * @param feedId 신고할 피드 ID
     */
    @POST("/api/v1/feeds/{feedId}/report")
    suspend fun reportFeed(
        @Path("feedId") feedId: Long,
    ): BaseResponse<Unit>

    /**
     * 회원 투표
     *
     * @param feedId 투표할 피드 ID
     * @param request 투표 선택 (YES or NO)
     */
    @POST("/api/v1/feeds/{feedId}/votes")
    suspend fun voteFeed(
        @Path("feedId") feedId: Long,
        @Body request: VoteRequest,
    ): BaseResponse<VoteResponse>

    /**
     * 비회원 투표
     *
     * @param feedId 투표할 피드 ID
     * @param request 투표 선택 (YES or NO)
     */
    @POST("/api/v1/feeds/{feedId}/votes/guest")
    @Headers("No-Authentication: true")
    suspend fun voteGuestFeed(
        @Path("feedId") feedId: Long,
        @Body request: VoteRequest,
    ): BaseResponse<VoteResponse>
}
