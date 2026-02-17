package com.sseotdabwa.buyornot.core.network.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("id")
    val id: Long,
    @SerialName("nickname")
    val nickname: String,
    @SerialName("profileImage")
    val profileImage: String,
    @SerialName("socialAccount")
    val socialAccount: String,
    @SerialName("email")
    val email: String,
)
