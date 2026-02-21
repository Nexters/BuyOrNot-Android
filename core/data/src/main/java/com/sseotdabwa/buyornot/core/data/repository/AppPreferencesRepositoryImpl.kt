package com.sseotdabwa.buyornot.core.data.repository

import com.sseotdabwa.buyornot.core.datastore.AppPreferencesDataSource
import com.sseotdabwa.buyornot.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * AppPreferencesRepository 구현체
 * 앱 전역 설정을 관리합니다.
 */
class AppPreferencesRepositoryImpl @Inject constructor(
    private val appPreferencesDataSource: AppPreferencesDataSource,
) : AppPreferencesRepository {
    override val hasRequestedNotificationPermission: Flow<Boolean> =
        appPreferencesDataSource.hasRequestedNotificationPermission

    override suspend fun updateNotificationPermissionRequested(requested: Boolean) {
        appPreferencesDataSource.updateNotificationPermissionRequested(requested)
    }
}
