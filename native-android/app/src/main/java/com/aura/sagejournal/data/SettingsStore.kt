package com.aura.sagejournal.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.prefs: DataStore<Preferences> by preferencesDataStore("aura_settings")

data class AuraSettings(
    val deepSea: Boolean = false,
    val motionIndex: Int = 1,
    val dailyReminder: Boolean = true,
    val showHud: Boolean = false,
    val onboarded: Boolean = false,
    val chimes: Boolean = true,
    val breathingSessions: Int = 0,
)

/** Scalar preferences. Entries live in Room; these do not warrant a table. */
class SettingsStore(context: Context) {
    private val store = context.applicationContext.prefs

    private object Keys {
        val deepSea = booleanPreferencesKey("deep_sea")
        val motion = intPreferencesKey("motion_index")
        val reminder = booleanPreferencesKey("daily_reminder")
        val hud = booleanPreferencesKey("show_hud")
        val onboarded = booleanPreferencesKey("onboarded")
        val chimes = booleanPreferencesKey("chimes")
        val sessions = intPreferencesKey("breathing_sessions")
    }

    val settings: Flow<AuraSettings> = store.data.map { p ->
        AuraSettings(
            deepSea = p[Keys.deepSea] ?: false,
            motionIndex = p[Keys.motion] ?: 1,
            dailyReminder = p[Keys.reminder] ?: true,
            showHud = p[Keys.hud] ?: false,
            onboarded = p[Keys.onboarded] ?: false,
            chimes = p[Keys.chimes] ?: true,
            breathingSessions = p[Keys.sessions] ?: 0,
        )
    }

    suspend fun setDeepSea(v: Boolean) = store.edit { it[Keys.deepSea] = v }
    suspend fun setMotion(v: Int) = store.edit { it[Keys.motion] = v }
    suspend fun setDailyReminder(v: Boolean) = store.edit { it[Keys.reminder] = v }
    suspend fun setShowHud(v: Boolean) = store.edit { it[Keys.hud] = v }
    suspend fun setOnboarded(v: Boolean) = store.edit { it[Keys.onboarded] = v }
    suspend fun setChimes(v: Boolean) = store.edit { it[Keys.chimes] = v }

    /** Counted when a session reaches a full cycle; feeds the Quiet Mind milestone. */
    suspend fun addBreathingSession() = store.edit {
        it[Keys.sessions] = (it[Keys.sessions] ?: 0) + 1
    }
}
