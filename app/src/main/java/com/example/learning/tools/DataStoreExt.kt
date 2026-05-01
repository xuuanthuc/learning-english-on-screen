package com.example.learning.tools

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.edit
object PrefKeys {
    val LEVELS = stringSetPreferencesKey("levels")
    val REPEAT_ENABLED = booleanPreferencesKey("repeat_enabled")
}
val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    val levelsFlow: Flow<List<String>> =
        context.dataStore.data.map { prefs ->
            prefs[PrefKeys.LEVELS]?.toList() ?: emptyList()
        }

    suspend fun saveLevels(levels: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[PrefKeys.LEVELS] = levels.toSet()
        }
    }

    val repeatEnabledFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[PrefKeys.REPEAT_ENABLED] ?: false
        }

    suspend fun setRepeatEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PrefKeys.REPEAT_ENABLED] = enabled
        }
    }
}