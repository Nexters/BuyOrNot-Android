package com.sseotdabwa.buyornot.domain.repository

import com.sseotdabwa.buyornot.domain.model.UserProfile

interface UserRepository {
    suspend fun getMyProfile(): UserProfile

    suspend fun deleteMyAccount()
}
