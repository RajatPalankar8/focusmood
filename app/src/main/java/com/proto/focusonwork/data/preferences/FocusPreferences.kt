package com.proto.focusonwork.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.focusDataStore by preferencesDataStore(name = "focus_preferences")

class FocusPreferences(private val context: Context) {
    private object Keys {
        val durationMinutes = intPreferencesKey("duration_minutes")
        val blockedPackages = stringSetPreferencesKey("blocked_packages")
    }

    val durationMinutes: Flow<Int> = context.focusDataStore.data.map {
        it[Keys.durationMinutes] ?: 45
    }

    val blockedPackages: Flow<Set<String>> = context.focusDataStore.data.map {
        it[Keys.blockedPackages] ?: emptySet()
    }

    suspend fun saveDuration(minutes: Int) {
        context.focusDataStore.edit { it[Keys.durationMinutes] = minutes }
    }

    suspend fun saveBlockedPackages(packages: Set<String>) {
        context.focusDataStore.edit { it[Keys.blockedPackages] = packages }
    }
}
