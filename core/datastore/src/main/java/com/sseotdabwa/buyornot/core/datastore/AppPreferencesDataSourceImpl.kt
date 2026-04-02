package com.sseotdabwa.buyornot.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appPreferencesDataStore by preferencesDataStore(name = "app_preferences")

/**
 * AppPreferencesDataSource 구현체
 *
 * 사용자 정보와 무관한 앱 레벨의 설정(알림 권한 등)을 DataStore로 관리합니다.
 */
@Singleton
class AppPreferencesDataSourceImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : AppPreferencesDataSource {
        private object Keys {
            val HAS_REQUESTED_NOTIFICATION_PERMISSION = booleanPreferencesKey("has_requested_notification_permission")
            val IS_FIRST_RUN = booleanPreferencesKey("is_first_run")
            val LAST_SOFT_UPDATE_SHOWN_TIME = longPreferencesKey("last_soft_update_shown_time")
        }

        override val preferences: Flow<AppPreferences> =
            context.appPreferencesDataStore.data.map { prefs ->
                AppPreferences(
                    hasRequestedNotificationPermission = prefs[Keys.HAS_REQUESTED_NOTIFICATION_PERMISSION] ?: false,
                    isFirstRun = prefs[Keys.IS_FIRST_RUN] ?: false,
                )
            }

        override val hasRequestedNotificationPermission: Flow<Boolean> =
            context.appPreferencesDataStore.data.map { prefs ->
                prefs[Keys.HAS_REQUESTED_NOTIFICATION_PERMISSION] ?: false
            }

        override val isFirstRun: Flow<Boolean> =
            context.appPreferencesDataStore.data.map { prefs ->
                prefs[Keys.IS_FIRST_RUN] ?: true
            }

        override val lastSoftUpdateShownTime: Flow<Long> =
            context.appPreferencesDataStore.data.map { prefs ->
                prefs[Keys.LAST_SOFT_UPDATE_SHOWN_TIME] ?: 0L
            }

        override suspend fun updateNotificationPermissionRequested(requested: Boolean) {
            context.appPreferencesDataStore.edit { prefs ->
                prefs[Keys.HAS_REQUESTED_NOTIFICATION_PERMISSION] = requested
            }
        }

        override suspend fun updateIsFirstRun(isFirstRun: Boolean) {
            context.appPreferencesDataStore.edit { prefs ->
                prefs[Keys.IS_FIRST_RUN] = isFirstRun
            }
        }

        override suspend fun updateLastSoftUpdateShownTime(timeMillis: Long) {
            context.appPreferencesDataStore.edit { prefs ->
                prefs[Keys.LAST_SOFT_UPDATE_SHOWN_TIME] = timeMillis
            }
        }
    }
