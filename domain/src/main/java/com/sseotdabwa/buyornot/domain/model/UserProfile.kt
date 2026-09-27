package com.sseotdabwa.buyornot.domain.model

data class UserProfile(
    val id: Long,
    // 회원가입 직후 닉네임을 설정하기 전까지 null이다.
    val nickname: String?,
    val profileImage: String,
    val socialAccount: String,
    val email: String,
)
