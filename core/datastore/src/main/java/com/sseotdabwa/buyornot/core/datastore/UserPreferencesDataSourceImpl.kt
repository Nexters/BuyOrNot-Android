package com.sseotdabwa.buyornot.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")

/**
 * UserPreferencesDataSource 구현체
 *
 * 사용자 프로필 및 인증 토큰을 DataStore로 관리합니다.
 */
@Singleton
class UserPreferencesDataSourceImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : UserPreferencesDataSource {
        private object Keys {
            val DISPLAY_NAME = stringPreferencesKey("display_name")
            val PROFILE_IMAGE_URL = stringPreferencesKey("profile_image_url")
            val ACCESS_TOKEN = stringPreferencesKey("access_token")
            val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
            val USER_TYPE = stringPreferencesKey("user_type")
        }

        override val preferences: Flow<UserPreferences> =
            context.userPreferencesDataStore.data.map { prefs ->
                UserPreferences(
                    displayName = prefs[Keys.DISPLAY_NAME] ?: UserPreferences().displayName,
                    profileImageUrl = prefs[Keys.PROFILE_IMAGE_URL] ?: UserPreferences().profileImageUrl,
                    accessToken = prefs[Keys.ACCESS_TOKEN] ?: UserPreferences().accessToken,
                    refreshToken = prefs[Keys.REFRESH_TOKEN] ?: UserPreferences().refreshToken,
                    userType =
                        prefs[Keys.USER_TYPE]?.let {
                            try {
                                UserType.valueOf(it)
                            } catch (e: IllegalArgumentException) {
                                UserPreferences().userType
                            }
                        } ?: UserPreferences().userType,
                )
            }

        override val accessToken: Flow<String> = context.userPreferencesDataStore.data.map { it[Keys.ACCESS_TOKEN] ?: "" }

        override val userType: Flow<UserType> =
            context.userPreferencesDataStore.data.map { prefs ->
                prefs[Keys.USER_TYPE]?.let {
                    try {
                        UserType.valueOf(it)
                    } catch (e: IllegalArgumentException) {
                        UserType.GUEST
                    }
                } ?: UserType.GUEST
            }

        override suspend fun updateDisplayName(newName: String) {
            context.userPreferencesDataStore.edit { prefs ->
                prefs[Keys.DISPLAY_NAME] = newName
            }
        }

        override suspend fun updateProfileImageUrl(newUrl: String) {
            context.userPreferencesDataStore.edit { prefs ->
                prefs[Keys.PROFILE_IMAGE_URL] = newUrl
            }
        }

        override suspend fun updateTokens(
            accessToken: String,
            refreshToken: String,
        ) {
            context.userPreferencesDataStore.edit { prefs ->
                prefs[Keys.ACCESS_TOKEN] = accessToken
                prefs[Keys.REFRESH_TOKEN] = refreshToken
            }
        }

        override suspend fun updateUserType(userType: UserType) {
            context.userPreferencesDataStore.edit { prefs ->
                prefs[Keys.USER_TYPE] = userType.name
            }
        }

        override suspend fun clearTokens() {
            context.userPreferencesDataStore.edit { prefs ->
                prefs.remove(Keys.ACCESS_TOKEN)
                prefs.remove(Keys.REFRESH_TOKEN)
                prefs[Keys.USER_TYPE] = UserType.GUEST.name
            }
        }
    }
