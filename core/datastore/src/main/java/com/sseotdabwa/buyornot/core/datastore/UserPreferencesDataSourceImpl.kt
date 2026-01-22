package com.sseotdabwa.buyornot.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesDataSource {

    private val displayNameKey = stringPreferencesKey("display_name")

    override val preferences: Flow<UserPreferences> =
        context.dataStore.data.map { prefs ->
            UserPreferences(displayName = prefs[displayNameKey] ?: UserPreferences().displayName)
        }

    override suspend fun updateDisplayName(newName: String) {
        context.dataStore.edit { prefs ->
            prefs[displayNameKey] = newName
        }
    }
}
