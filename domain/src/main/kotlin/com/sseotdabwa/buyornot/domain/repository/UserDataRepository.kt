package com.sseotdabwa.buyornot.domain.repository

import com.sseotdabwa.buyornot.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserDataRepository {
    val userProfile: Flow<UserProfile>

    suspend fun updateUserName(newName: String)
}
