package com.sseotdabwa.buyornot.core.data.repository

import com.sseotdabwa.buyornot.core.datastore.UserPreferencesDataSource
import com.sseotdabwa.buyornot.domain.model.ImageSource
import com.sseotdabwa.buyornot.domain.model.UserPreferences
import com.sseotdabwa.buyornot.domain.model.UserToken
import com.sseotdabwa.buyornot.domain.model.UserType
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import com.sseotdabwa.buyornot.core.datastore.UserPreferences as DatastoreUserPreferences
import com.sseotdabwa.buyornot.core.datastore.UserType as DatastoreUserType

/**
 * UserPreferencesRepository 구현체
 * DataStore의 UserType과 Domain의 UserType을 매핑
 */
class UserPreferencesRepositoryImpl @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
) : UserPreferencesRepository {
    override val userPreferences: Flow<UserPreferences> =
        userPreferencesDataSource.preferences.map { it.toDomain() }

    override val userToken: Flow<UserToken> =
        userPreferencesDataSource.preferences.map { it.toTokenDomain() }

    override val userType: Flow<UserType> =
        userPreferencesDataSource.userType.map { it.toDomain() }

    override val userId: Flow<Long> = userPreferencesDataSource.userId

    override suspend fun updateUserType(userType: UserType) {
        userPreferencesDataSource.updateUserType(userType.toDatastore())
    }

    override suspend fun updateUserId(userId: Long) {
        userPreferencesDataSource.updateUserId(userId)
    }

    override suspend fun updateDisplayName(newName: String) {
        userPreferencesDataSource.updateDisplayName(newName)
    }

    override suspend fun updateProfileImage(newImage: ImageSource) {
        userPreferencesDataSource.updateProfileImageUrl(newImage.url)
    }
}

/**
 * DataStore UserPreferences → Domain UserPreferences
 */
private fun DatastoreUserPreferences.toDomain(): UserPreferences =
    UserPreferences(
        displayName = displayName,
        profileImage = ImageSource(profileImageUrl),
        userType = userType.toDomain(),
    )

/**
 * DataStore UserPreferences → Domain UserToken
 */
private fun DatastoreUserPreferences.toTokenDomain(): UserToken =
    UserToken(
        accessToken = accessToken,
        refreshToken = refreshToken,
    )

/**
 * DataStore UserType → Domain UserType
 */
private fun DatastoreUserType.toDomain(): UserType =
    when (this) {
        DatastoreUserType.NOT_LOGGED_IN -> UserType.NOT_LOGGED_IN
        DatastoreUserType.SOCIAL -> UserType.SOCIAL
    }

/**
 * Domain UserType → DataStore UserType
 */
private fun UserType.toDatastore(): DatastoreUserType =
    when (this) {
        UserType.NOT_LOGGED_IN -> DatastoreUserType.NOT_LOGGED_IN
        UserType.SOCIAL -> DatastoreUserType.SOCIAL
    }
