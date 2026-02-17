package com.sseotdabwa.buyornot.core.data.repository

import com.sseotdabwa.buyornot.core.network.api.UserApiService
import com.sseotdabwa.buyornot.core.network.dto.response.User
import com.sseotdabwa.buyornot.domain.model.UserProfile
import com.sseotdabwa.buyornot.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService,
) : UserRepository {
    override suspend fun getMyProfile(): UserProfile {
        val response = userApiService.getMyProfile()
        if (response.errorCode != null) {
            throw Exception(response.message)
        }
        return response.data.toDomain()
    }

    override suspend fun deleteMyAccount() {
        val response = userApiService.deleteMyAccount()
        if (response.errorCode != null) {
            throw Exception(response.message)
        }
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
