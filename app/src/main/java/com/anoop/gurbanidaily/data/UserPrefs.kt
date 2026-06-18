package com.anoop.gurbanidaily.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "gurbani_prefs")

enum class ReminderSlot(val key: String, val label: String, val defaultHour: Int, val defaultMinute: Int) {
    Morning("morning", "Amritvela (morning)", 5, 0),
    Midday("midday", "Midday", 12, 30),
    Evening("evening", "Evening (Rehras)", 18, 30)
}

data class ReminderState(val enabled: Boolean, val hour: Int, val minute: Int)

class UserPrefs(private val context: Context) {

    private val keyDark = booleanPreferencesKey("dark_mode")
    private val keyDynamic = booleanPreferencesKey("dynamic_color")
    private val keyFontScale = floatPreferencesKey("font_scale")
    private val keyFavorites = stringSetPreferencesKey("favorites")
    private val keyHistoryOrdered = stringSetPreferencesKey("history_ordered")
    private val keyOnboarded = booleanPreferencesKey("onboarded")

    private fun slotEnabledKey(slot: ReminderSlot) = booleanPreferencesKey("reminder_${slot.key}_on")
    private fun slotHourKey(slot: ReminderSlot) = intPreferencesKey("reminder_${slot.key}_h")
    private fun slotMinuteKey(slot: ReminderSlot) = intPreferencesKey("reminder_${slot.key}_m")

    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[keyDark] ?: false }
    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { it[keyDynamic] ?: true }
    val fontScale: Flow<Float> = context.dataStore.data.map { it[keyFontScale] ?: 1.0f }
    val favorites: Flow<Set<String>> = context.dataStore.data.map { it[keyFavorites] ?: emptySet() }
    val history: Flow<List<String>> = context.dataStore.data.map { prefs -> prefs.historyList() }
    val onboarded: Flow<Boolean> = context.dataStore.data.map { it[keyOnboarded] ?: false }

    fun reminder(slot: ReminderSlot): Flow<ReminderState> = context.dataStore.data.map { prefs ->
        ReminderState(
            enabled = prefs[slotEnabledKey(slot)] ?: false,
            hour = prefs[slotHourKey(slot)] ?: slot.defaultHour,
            minute = prefs[slotMinuteKey(slot)] ?: slot.defaultMinute
        )
    }

    suspend fun setDarkMode(v: Boolean) = context.dataStore.edit { it[keyDark] = v }
    suspend fun setDynamicColor(v: Boolean) = context.dataStore.edit { it[keyDynamic] = v }
    suspend fun setFontScale(v: Float) = context.dataStore.edit { it[keyFontScale] = v }
    suspend fun setOnboarded(v: Boolean) = context.dataStore.edit { it[keyOnboarded] = v }

    suspend fun setReminderEnabled(slot: ReminderSlot, on: Boolean) = context.dataStore.edit {
        it[slotEnabledKey(slot)] = on
    }
    suspend fun setReminderTime(slot: ReminderSlot, hour: Int, minute: Int) =
        context.dataStore.edit {
            it[slotHourKey(slot)] = hour
            it[slotMinuteKey(slot)] = minute
        }

    suspend fun toggleFavorite(id: String) = context.dataStore.edit { prefs ->
        val current = prefs[keyFavorites] ?: emptySet()
        prefs[keyFavorites] = if (id in current) current - id else current + id
    }

    suspend fun pushHistory(id: String) = context.dataStore.edit { prefs ->
        val ordered = prefs.historyList().toMutableList()
        ordered.remove(id)
        ordered.add(0, id)
        while (ordered.size > MAX_HISTORY) ordered.removeAt(ordered.size - 1)
        prefs[keyHistoryOrdered] = ordered.mapIndexed { i, v -> "$i|$v" }.toSet()
    }

    suspend fun clearHistory() = context.dataStore.edit { it[keyHistoryOrdered] = emptySet() }

    private fun Preferences.historyList(): List<String> {
        val raw = this[keyHistoryOrdered] ?: emptySet()
        return raw.mapNotNull { entry ->
            val parts = entry.split("|", limit = 2)
            if (parts.size == 2) parts[0].toIntOrNull()?.let { i -> i to parts[1] } else null
        }.sortedBy { it.first }.map { it.second }
    }

    companion object {
        const val MAX_HISTORY = 30
    }
}
