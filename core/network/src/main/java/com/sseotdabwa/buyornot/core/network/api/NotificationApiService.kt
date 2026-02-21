package com.sseotdabwa.buyornot.core.network.api

import com.sseotdabwa.buyornot.core.network.dto.response.BaseResponse
import com.sseotdabwa.buyornot.core.network.dto.response.NotificationResponse
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApiService {
    @GET("/api/v1/notifications")
    suspend fun getNotifications(
        @Query("type") type: String?,
    ): BaseResponse<List<NotificationResponse>>

    @PATCH("/api/v1/notifications/{notificationId}/read")
    suspend fun markAsRead(
        @Path("notificationId") notificationId: Long,
    ): BaseResponse<Unit>
}
