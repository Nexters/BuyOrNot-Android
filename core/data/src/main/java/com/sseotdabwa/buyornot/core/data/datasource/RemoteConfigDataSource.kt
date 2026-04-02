package com.sseotdabwa.buyornot.core.data.datasource

import com.sseotdabwa.buyornot.domain.model.AppUpdateInfo

interface RemoteConfigDataSource {
    suspend fun fetchAppUpdateConfig(): AppUpdateInfo
}
