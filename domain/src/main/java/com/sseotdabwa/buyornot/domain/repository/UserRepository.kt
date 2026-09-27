package com.sseotdabwa.buyornot.domain.repository

import com.sseotdabwa.buyornot.domain.model.BlockedUser
import com.sseotdabwa.buyornot.domain.model.UserProfile

interface UserRepository {
    suspend fun getMyProfile(): UserProfile

    suspend fun deleteMyAccount()

    /**
     * 닉네임/프로필 이미지를 수정한다. null인 값은 변경하지 않는다.
     * 회원가입 직후 닉네임 최초 설정에도 사용한다.
     *
     * @throws com.sseotdabwa.buyornot.domain.exception.ApiException 닉네임 정책 위반, 중복 등 서버가 거절한 경우
     */
    suspend fun updateProfile(
        nickname: String? = null,
        profileImage: String? = null,
    ): UserProfile

    suspend fun updateFcmToken(fcmToken: String)

    suspend fun getBlockedUsers(): List<BlockedUser>

    suspend fun blockUser(userId: Long)

    suspend fun unblockUser(userId: Long)

    suspend fun notifyAppOpened()
}
