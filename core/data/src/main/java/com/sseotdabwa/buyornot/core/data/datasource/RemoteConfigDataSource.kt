package com.sseotdabwa.buyornot.core.data.datasource

import com.sseotdabwa.buyornot.domain.model.AppUpdateInfo

interface RemoteConfigDataSource {
    suspend fun fetchAppUpdateConfig(): AppUpdateInfo

    /** AI 초안 프롬프트의 지시문. 원격 값이 비어 있거나 가져오지 못하면 기본값을 쓴다. */
    suspend fun fetchAiDraftInstruction(): String
}
