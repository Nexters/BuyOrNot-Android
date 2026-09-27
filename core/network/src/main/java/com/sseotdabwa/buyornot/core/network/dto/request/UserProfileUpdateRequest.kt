package com.sseotdabwa.buyornot.core.network.dto.request

import kotlinx.serialization.Serializable

/**
 * 프로필 수정 요청 (닉네임 최초 설정 겸용).
 * null인 필드는 직렬화에서 빠지므로 서버가 해당 값을 변경하지 않는다.
 */
@Serializable
data class UserProfileUpdateRequest(
    val nickname: String? = null,
    val profileImage: String? = null,
)
