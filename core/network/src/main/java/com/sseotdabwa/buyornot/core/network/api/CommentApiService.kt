package com.sseotdabwa.buyornot.core.network.api

import com.sseotdabwa.buyornot.core.network.dto.request.CommentRequest
import com.sseotdabwa.buyornot.core.network.dto.response.BaseResponse
import com.sseotdabwa.buyornot.core.network.dto.response.CommentCreateResponse
import com.sseotdabwa.buyornot.core.network.dto.response.CommentListResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 피드 댓글 API. 앱은 회원 전용이라 비회원(`/guest`) 작성·삭제 API는 두지 않는다.
 */
interface CommentApiService {
    /**
     * 댓글 목록 조회 (커서 페이징). 로그인 상태면 토큰으로 `isMine`을 판단한다.
     *
     * @param cursor 이전 페이지 마지막 댓글 id (첫 페이지는 생략)
     * @param size 페이지 크기 (기본값 20, 최대 50)
     * @param sort REGISTERED(등록순, 기본값) 또는 LATEST(최신순)
     */
    @GET("/api/v1/feeds/{feedId}/comments")
    suspend fun getComments(
        @Path("feedId") feedId: Long,
        @Query("cursor") cursor: Long? = null,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String? = null,
    ): BaseResponse<CommentListResponse>

    /** 회원 댓글 작성. 내용은 공백 제거 후 1~300자. */
    @POST("/api/v1/feeds/{feedId}/comments")
    suspend fun createComment(
        @Path("feedId") feedId: Long,
        @Body request: CommentRequest,
    ): BaseResponse<CommentCreateResponse>

    /** 회원 댓글 삭제. 본인 댓글만 가능하다. */
    @DELETE("/api/v1/feeds/{feedId}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("feedId") feedId: Long,
        @Path("commentId") commentId: Long,
    ): BaseResponse<Unit>

    /** 댓글 신고. 본인 댓글은 신고할 수 없다(COMMENT_008). */
    @POST("/api/v1/feeds/{feedId}/comments/{commentId}/report")
    suspend fun reportComment(
        @Path("feedId") feedId: Long,
        @Path("commentId") commentId: Long,
    ): BaseResponse<Unit>
}
