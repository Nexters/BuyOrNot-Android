package com.sseotdabwa.buyornot.domain.repository

import com.sseotdabwa.buyornot.domain.model.AppUpdateInfo

interface AppUpdateRepository {
    suspend fun getAppUpdateInfo(): AppUpdateInfo
}
