package com.sseotdabwa.buyornot.core.data.repository

import com.sseotdabwa.buyornot.core.network.api.UserApiService
import com.sseotdabwa.buyornot.core.network.dto.request.FcmTokenRequest
import com.sseotdabwa.buyornot.core.network.dto.request.UserProfileUpdateRequest
import com.sseotdabwa.buyornot.core.network.dto.response.User
import com.sseotdabwa.buyornot.core.network.dto.response.errorCodeOrNull
import com.sseotdabwa.buyornot.core.network.dto.response.getOrThrow
import com.sseotdabwa.buyornot.domain.exception.ApiException
import com.sseotdabwa.buyornot.domain.model.BlockedUser
import com.sseotdabwa.buyornot.domain.model.UserProfile
import com.sseotdabwa.buyornot.domain.repository.UserRepository
import retrofit2.HttpException
import javax.inject.Inject
import com.sseotdabwa.buyornot.core.network.dto.response.BlockedUser as BlockedUserResponse

class UserRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService,
) : UserRepository {
    override suspend fun getMyProfile(): UserProfile = userApiService.getMyProfile().getOrThrow().toDomain()

    override suspend fun deleteMyAccount() {
        userApiService.deleteMyAccount().getOrThrow()
    }

    override suspend fun updateProfile(
        nickname: String?,
        profileImage: String?,
    ): UserProfile =
        try {
            userApiService
                .updateProfile(UserProfileUpdateRequest(nickname = nickname, profileImage = profileImage))
                .getOrThrow()
                .toDomain()
        } catch (e: HttpException) {
            throw ApiException(code = e.errorCodeOrNull(), message = e.message(), cause = e)
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

    override suspend fun notifyAppOpened() {
        userApiService.notifyAppOpened().getOrThrow()
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
