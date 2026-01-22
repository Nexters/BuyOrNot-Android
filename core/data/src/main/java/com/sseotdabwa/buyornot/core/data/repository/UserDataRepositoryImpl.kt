package com.sseotdabwa.buyornot.core.data.repository

import com.sseotdabwa.buyornot.core.datastore.UserPreferencesDataSource
import com.sseotdabwa.buyornot.domain.model.UserProfile
import com.sseotdabwa.buyornot.domain.repository.UserDataRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class UserDataRepositoryImpl @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource
) : UserDataRepository {
    override val userProfile: Flow<UserProfile> =
        userPreferencesDataSource.preferences.map { prefs ->
            UserProfile(displayName = prefs.displayName)
        }

    override suspend fun updateUserName(newName: String) {
        userPreferencesDataSource.updateDisplayName(newName)
    }
}
