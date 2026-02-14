package com.sseotdabwa.buyornot.core.network.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    @SerialName("data")
    val data: TokenData,
    @SerialName("message")
    val message: String,
    @SerialName("status")
    val status: String,
)

@Serializable
data class TokenData(
    @SerialName("accessToken")
    val accessToken: String,
    @SerialName("refreshToken")
    val refreshToken: String,
    @SerialName("tokenType")
    val tokenType: String,
    @SerialName("user")
    val user: User,
)

@Serializable
data class User(
    @SerialName("id")
    val id: Int,
    @SerialName("nickname")
    val nickname: String,
    @SerialName("profileImage")
    val profileImage: String,
    @SerialName("socialAccount")
    val socialAccount: String,
    @SerialName("email")
    val email: String,
)
