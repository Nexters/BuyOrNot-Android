package com.sseotdabwa.buyornot.core.data.repository

import com.sseotdabwa.buyornot.core.datastore.UserPreferencesDataSource
import com.sseotdabwa.buyornot.domain.model.UserType
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import com.sseotdabwa.buyornot.core.datastore.UserType as DatastoreUserType

/**
 * UserPreferencesRepository 구현체
 * DataStore의 UserType과 Domain의 UserType을 매핑
 */
class UserPreferencesRepositoryImpl @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
) : UserPreferencesRepository {
    override val userType: Flow<UserType> =
        userPreferencesDataSource.userType.map { it.toDomain() }

    override suspend fun updateUserType(userType: UserType) {
        userPreferencesDataSource.updateUserType(userType.toDatastore())
    }
}

/**
 * DataStore UserType → Domain UserType
 */
private fun DatastoreUserType.toDomain(): UserType =
    when (this) {
        DatastoreUserType.GUEST -> UserType.GUEST
        DatastoreUserType.SOCIAL -> UserType.SOCIAL
    }

/**
 * Domain UserType → DataStore UserType
 */
private fun UserType.toDatastore(): DatastoreUserType =
    when (this) {
        UserType.GUEST -> DatastoreUserType.GUEST
        UserType.SOCIAL -> DatastoreUserType.SOCIAL
    }
