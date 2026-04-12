package com.sseotdabwa.buyornot.core.data.repository

import com.sseotdabwa.buyornot.core.data.datasource.RemoteConfigDataSource
import com.sseotdabwa.buyornot.domain.model.AppUpdateInfo
import com.sseotdabwa.buyornot.domain.repository.AppUpdateRepository
import javax.inject.Inject

class AppUpdateRepositoryImpl @Inject constructor(
    private val remoteConfigDataSource: RemoteConfigDataSource,
) : AppUpdateRepository {
    override suspend fun getAppUpdateInfo(): AppUpdateInfo = remoteConfigDataSource.fetchAppUpdateConfig()
}
