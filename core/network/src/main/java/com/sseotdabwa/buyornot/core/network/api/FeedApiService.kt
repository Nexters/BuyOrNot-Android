package com.sseotdabwa.buyornot.core.network.api

import com.sseotdabwa.buyornot.core.network.dto.request.FeedRequest
import com.sseotdabwa.buyornot.core.network.dto.request.PresignedUrlRequest
import com.sseotdabwa.buyornot.core.network.dto.response.BaseResponse
import com.sseotdabwa.buyornot.core.network.dto.response.FeedResponse
import com.sseotdabwa.buyornot.core.network.dto.response.PresignedUrlResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Url

interface FeedApiService {
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

    @POST("/api/v1/feeds")
    suspend fun createFeed(
        @Body request: FeedRequest,
    ): BaseResponse<FeedResponse>
}
