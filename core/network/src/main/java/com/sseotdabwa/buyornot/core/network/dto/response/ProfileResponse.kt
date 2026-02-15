package com.sseotdabwa.buyornot.core.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    val id: Long,
    val nickname: String,
    val profileImage: String,
    val socialAccount: String,
    val email: String,
)
