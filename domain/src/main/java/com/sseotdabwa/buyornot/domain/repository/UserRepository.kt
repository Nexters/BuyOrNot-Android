package com.sseotdabwa.buyornot.domain.repository

import com.sseotdabwa.buyornot.domain.model.BlockedUser
import com.sseotdabwa.buyornot.domain.model.UserProfile

interface UserRepository {
    suspend fun getMyProfile(): UserProfile

    suspend fun deleteMyAccount()

    suspend fun updateFcmToken(fcmToken: String)

    suspend fun getBlockedUsers(): List<BlockedUser>

    suspend fun blockUser(userId: Long)

    suspend fun unblockUser(userId: Long)
}
