package com.sseotdabwa.buyornot.core.network.api

import com.sseotdabwa.buyornot.core.network.dto.request.FcmTokenRequest
import com.sseotdabwa.buyornot.core.network.dto.response.BaseResponse
import com.sseotdabwa.buyornot.core.network.dto.response.BlockedUser
import com.sseotdabwa.buyornot.core.network.dto.response.User
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApiService {
    @GET("/api/v1/users/me")
    suspend fun getMyProfile(): BaseResponse<User>

    @DELETE("/api/v1/users/me")
    suspend fun deleteMyAccount(): BaseResponse<Unit>

    @PATCH("/api/v1/users/fcm")
    suspend fun updateFcmToken(
        @Body request: FcmTokenRequest,
    ): BaseResponse<Unit>

    @GET("/api/v1/users/blocks")
    suspend fun getBlockedUsers(): BaseResponse<List<BlockedUser>>

    @POST("/api/v1/users/blocks/{userId}")
    suspend fun blockUser(
        @Path("userId") userId: Long,
    ): BaseResponse<Unit>

    @DELETE("/api/v1/users/blocks/{userId}")
    suspend fun unblockUser(
        @Path("userId") userId: Long,
    ): BaseResponse<Unit>
}
