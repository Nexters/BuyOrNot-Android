package com.sseotdabwa.buyornot.core.network.api

import com.sseotdabwa.buyornot.core.network.dto.request.GoogleLoginRequest
import com.sseotdabwa.buyornot.core.network.dto.request.KakaoLoginRequest
import com.sseotdabwa.buyornot.core.network.dto.request.RefreshRequest
import com.sseotdabwa.buyornot.core.network.dto.response.BaseResponse
import com.sseotdabwa.buyornot.core.network.dto.response.TokenData
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("/api/v1/auth/kakao/login")
    suspend fun kakaoLogin(
        @Body request: KakaoLoginRequest,
    ): BaseResponse<TokenData>

    @POST("/api/v1/auth/google/login")
    suspend fun googleLogin(
        @Body request: GoogleLoginRequest,
    ): BaseResponse<TokenData>

    @POST("/api/v1/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshRequest,
    ): BaseResponse<TokenData>

    @POST("/api/v1/auth/logout")
    suspend fun logout(
        @Body request: RefreshRequest,
    ): BaseResponse<Unit>
}
