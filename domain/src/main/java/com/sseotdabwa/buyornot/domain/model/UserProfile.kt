package com.sseotdabwa.buyornot.domain.model

data class UserProfile(
    val id: Long,
    val nickname: String,
    val profileImage: String,
    val socialAccount: String,
    val email: String,
)
