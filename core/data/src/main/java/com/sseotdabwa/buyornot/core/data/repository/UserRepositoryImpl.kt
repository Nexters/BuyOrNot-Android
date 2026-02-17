package com.sseotdabwa.buyornot.core.data.repository

import com.sseotdabwa.buyornot.core.network.api.UserApiService
import com.sseotdabwa.buyornot.core.network.dto.response.User
import com.sseotdabwa.buyornot.core.network.dto.response.getOrThrow
import com.sseotdabwa.buyornot.domain.model.UserProfile
import com.sseotdabwa.buyornot.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService,
) : UserRepository {
    override suspend fun getMyProfile(): UserProfile = userApiService.getMyProfile().getOrThrow().toDomain()

    override suspend fun deleteMyAccount() {
        userApiService.deleteMyAccount().getOrThrow()
    }

    private fun User.toDomain(): UserProfile =
        UserProfile(
            id = id,
            nickname = nickname,
            profileImage = profileImage,
            socialAccount = socialAccount,
            email = email,
        )
}
