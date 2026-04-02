package com.sseotdabwa.buyornot.core.data.datasource

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.sseotdabwa.buyornot.domain.model.AppUpdateInfo
import com.sseotdabwa.buyornot.domain.model.UpdateStrategy
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigDataSourceImpl
    @Inject
    constructor() : RemoteConfigDataSource {
        private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

        override suspend fun fetchAppUpdateConfig(): AppUpdateInfo {
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
                    ),
                ).await()

            remoteConfig.fetchAndActivate().await()

            val latestVersion = remoteConfig.getLong(KEY_LATEST_VERSION).toInt()
            val minimumVersion = remoteConfig.getLong(KEY_MINIMUM_VERSION).toInt()
            val strategyRaw = remoteConfig.getString(KEY_UPDATE_STRATEGY)
            val updateStrategy =
                runCatching { UpdateStrategy.valueOf(strategyRaw) }.getOrDefault(UpdateStrategy.NONE)

            return AppUpdateInfo(
                latestVersion = latestVersion,
                minimumVersion = minimumVersion,
                updateStrategy = updateStrategy,
            )
        }

        companion object {
            private const val KEY_LATEST_VERSION = "android_latest_version"
            private const val KEY_MINIMUM_VERSION = "android_minimum_version"
            private const val KEY_UPDATE_STRATEGY = "android_update_strategy"
            private const val DEFAULT_VERSION = 1L
            private const val FETCH_INTERVAL_SECONDS = 3600L
        }
    }
