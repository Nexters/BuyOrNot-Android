package com.sseotdabwa.buyornot.core.data.datasource

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.sseotdabwa.buyornot.core.data.draft.PostDraftPrompt
import com.sseotdabwa.buyornot.domain.model.AppUpdateInfo
import com.sseotdabwa.buyornot.domain.model.UpdateStrategy
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigDataSourceImpl @Inject constructor() : RemoteConfigDataSource {
    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
    private val initMutex = Mutex()
    private var isInitialized = false

    override suspend fun fetchAppUpdateConfig(): AppUpdateInfo {
        fetchAndActivate()

        val latestVersion = remoteConfig.getLong(KEY_LATEST_VERSION).toInt()
        val minimumVersion = remoteConfig.getLong(KEY_MINIMUM_VERSION).toInt()
        val strategyRaw = remoteConfig.getString(KEY_UPDATE_STRATEGY)
        val updateStrategy =
            runCatching { UpdateStrategy.valueOf(strategyRaw) }.getOrDefault(UpdateStrategy.NONE)

        Timber.tag(TAG).d(
            "Remote Config values — " +
                "latestVersion=$latestVersion, " +
                "minimumVersion=$minimumVersion, " +
                "strategyRaw=$strategyRaw, " +
                "resolvedStrategy=$updateStrategy",
        )

        return AppUpdateInfo(
            latestVersion = latestVersion,
            minimumVersion = minimumVersion,
            updateStrategy = updateStrategy,
        )
    }

    override suspend fun fetchAiDraftInstruction(): String {
        // 사용자가 버튼을 누른 뒤 기다리는 중이라 네트워크가 느리면 캐시·기본값으로 진행한다.
        withTimeoutOrNull(AI_DRAFT_FETCH_TIMEOUT_MILLIS) { fetchAndActivate() }
        return remoteConfig.getString(KEY_AI_DRAFT_INSTRUCTION).ifBlank { PostDraftPrompt.DEFAULT_INSTRUCTION }
    }

    // setDefaultsAsync는 기본값 전체를 교체하므로 모든 키의 기본값을 한 번에 등록한다.
    private suspend fun ensureInitialized() {
        initMutex.withLock {
            if (isInitialized) return
            val settings =
                FirebaseRemoteConfigSettings
                    .Builder()
                    .setMinimumFetchIntervalInSeconds(FETCH_INTERVAL_SECONDS)
                    .build()
            remoteConfig.setConfigSettingsAsync(settings).await()
            remoteConfig
                .setDefaultsAsync(
                    mapOf(
                        KEY_LATEST_VERSION to DEFAULT_VERSION,
                        KEY_MINIMUM_VERSION to DEFAULT_VERSION,
                        KEY_UPDATE_STRATEGY to UpdateStrategy.NONE.name,
                        KEY_AI_DRAFT_INSTRUCTION to PostDraftPrompt.DEFAULT_INSTRUCTION,
                    ),
                ).await()
            isInitialized = true
        }
    }

    private suspend fun fetchAndActivate() {
        ensureInitialized()
        val activated =
            runCatching { remoteConfig.fetchAndActivate().await() }
                .onFailure { Timber.tag(TAG).w(it, "fetchAndActivate failed, using cached/default values") }
                .getOrDefault(false)
        Timber.tag(TAG).d("fetchAndActivate: activated=$activated")
    }

    companion object {
        private const val TAG = "RemoteConfig"
        private const val KEY_LATEST_VERSION = "android_latest_version"
        private const val KEY_MINIMUM_VERSION = "android_minimum_version"
        private const val KEY_UPDATE_STRATEGY = "android_update_strategy"
        private const val KEY_AI_DRAFT_INSTRUCTION = "ai_draft_instruction"
        private const val AI_DRAFT_FETCH_TIMEOUT_MILLIS = 3_000L
        private const val DEFAULT_VERSION = 1L
        private const val FETCH_INTERVAL_SECONDS = 3600L
    }
}
