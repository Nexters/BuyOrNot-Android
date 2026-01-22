package com.sseotdabwa.buyornot.core.datastore

import kotlinx.coroutines.flow.Flow

interface UserPreferencesDataSource {
    val preferences: Flow<UserPreferences>

    suspend fun updateDisplayName(newName: String)
}
