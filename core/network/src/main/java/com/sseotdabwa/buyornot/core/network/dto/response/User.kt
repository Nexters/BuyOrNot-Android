package com.sseotdabwa.buyornot.core.network.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("id")
    val id: Long,
    // 회원가입 직후 닉네임을 설정하기 전까지 null로 내려온다.
    @SerialName("nickname")
    val nickname: String? = null,
    @SerialName("profileImage")
    val profileImage: String,
    @SerialName("socialAccount")
    val socialAccount: String,
    @SerialName("email")
    val email: String,
)
