package com.sseotdabwa.buyornot.core.data.repository

import com.sseotdabwa.buyornot.core.network.api.UserApiService
import com.sseotdabwa.buyornot.core.network.dto.request.FcmTokenRequest
import com.sseotdabwa.buyornot.core.network.dto.response.User
import com.sseotdabwa.buyornot.core.network.dto.response.getOrThrow
import com.sseotdabwa.buyornot.domain.model.BlockedUser
import com.sseotdabwa.buyornot.domain.model.UserProfile
import com.sseotdabwa.buyornot.domain.repository.UserRepository
import javax.inject.Inject
import com.sseotdabwa.buyornot.core.network.dto.response.BlockedUser as BlockedUserResponse

class UserRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService,
) : UserRepository {
    override suspend fun getMyProfile(): UserProfile = userApiService.getMyProfile().getOrThrow().toDomain()

    override suspend fun deleteMyAccount() {
        userApiService.deleteMyAccount().getOrThrow()
    }

    override suspend fun updateFcmToken(fcmToken: String) {
        userApiService.updateFcmToken(FcmTokenRequest(fcmToken)).getOrThrow()
    }

    override suspend fun getBlockedUsers(): List<BlockedUser> = userApiService.getBlockedUsers().getOrThrow().map { it.toDomain() }

    override suspend fun blockUser(userId: Long) {
        userApiService.blockUser(userId).getOrThrow()
    }

    override suspend fun unblockUser(userId: Long) {
        userApiService.unblockUser(userId).getOrThrow()
    }

    private fun User.toDomain(): UserProfile =
        UserProfile(
            id = id,
            nickname = nickname,
            profileImage = profileImage,
            socialAccount = socialAccount,
            email = email,
        )

    private fun BlockedUserResponse.toDomain(): BlockedUser =
        BlockedUser(
            userId = userId,
            nickname = nickname,
            profileImage = profileImage,
        )
}
