package com.sseotdabwa.buyornot.core.network.api

import com.sseotdabwa.buyornot.core.network.dto.response.BaseResponse
import com.sseotdabwa.buyornot.core.network.dto.response.ProfileResponse
import retrofit2.http.DELETE
import retrofit2.http.GET

interface UserApiService {
    @GET("/api/v1/users/me")
    suspend fun getMyProfile(): BaseResponse<ProfileResponse>

    @DELETE("/api/v1/users/me")
    suspend fun deleteMyAccount(): BaseResponse<Unit>
}
