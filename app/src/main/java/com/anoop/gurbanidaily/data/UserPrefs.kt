package com.anoop.gurbanidaily.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    private val keyFavorites = stringSetPreferencesKey("favorites_ids")
    private val keyHistoryOrdered = stringSetPreferencesKey("history_ordered_ids")
    private val keyStreak = intPreferencesKey("streak_days")
    private val keyLastOpenedDate = stringPreferencesKey("last_opened_date")
    private val keyJournal = stringPreferencesKey("journal_json")
    private val keyLastUpdateCheck = longPreferencesKey("last_update_check_ms")

    private fun slotEnabledKey(slot: ReminderSlot) = booleanPreferencesKey("reminder_${slot.key}_on")
    private fun slotHourKey(slot: ReminderSlot) = intPreferencesKey("reminder_${slot.key}_h")
    private fun slotMinuteKey(slot: ReminderSlot) = intPreferencesKey("reminder_${slot.key}_m")

    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[keyDark] ?: false }
    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { it[keyDynamic] ?: false }
    val fontScale: Flow<Float> = context.dataStore.data.map { it[keyFontScale] ?: 1.0f }
    val favorites: Flow<Set<Long>> = context.dataStore.data.map {
        (it[keyFavorites] ?: emptySet()).mapNotNull { s -> s.toLongOrNull() }.toSet()
    }
    val history: Flow<List<Long>> = context.dataStore.data.map { prefs -> prefs.historyList() }
    val streak: Flow<Int> = context.dataStore.data.map { it[keyStreak] ?: 0 }
    val lastUpdateCheck: Flow<Long> = context.dataStore.data.map { it[keyLastUpdateCheck] ?: 0L }
    val journal: Flow<Map<Long, String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[keyJournal] ?: return@map emptyMap()
        runCatching {
            val obj = JSONObject(raw)
            obj.keys().asSequence()
                .mapNotNull { k -> k.toLongOrNull()?.let { it to obj.optString(k, "") } }
                .toMap()
        }.getOrDefault(emptyMap())
    }

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

    suspend fun setReminderEnabled(slot: ReminderSlot, on: Boolean) =
        context.dataStore.edit { it[slotEnabledKey(slot)] = on }
    suspend fun setReminderTime(slot: ReminderSlot, hour: Int, minute: Int) =
        context.dataStore.edit {
            it[slotHourKey(slot)] = hour
            it[slotMinuteKey(slot)] = minute
        }

    suspend fun toggleFavorite(id: Long) = context.dataStore.edit { prefs ->
        val current = prefs[keyFavorites] ?: emptySet()
        val idStr = id.toString()
        prefs[keyFavorites] = if (idStr in current) current - idStr else current + idStr
    }

    suspend fun pushHistory(id: Long) = context.dataStore.edit { prefs ->
        val ordered = prefs.historyList().toMutableList()
        ordered.remove(id)
        ordered.add(0, id)
        while (ordered.size > MAX_HISTORY) ordered.removeAt(ordered.size - 1)
        prefs[keyHistoryOrdered] =
            ordered.mapIndexed { i, v -> "$i|$v" }.toSet()
    }

    suspend fun clearHistory() = context.dataStore.edit { it[keyHistoryOrdered] = emptySet() }

    suspend fun touchStreak() = context.dataStore.edit { prefs ->
        val today = todayKey()
        val last = prefs[keyLastOpenedDate]
        val current = prefs[keyStreak] ?: 0
        val newStreak = when (last) {
            today -> current.coerceAtLeast(1)
            yesterdayKey() -> current + 1
            else -> 1
        }
        prefs[keyStreak] = newStreak
        prefs[keyLastOpenedDate] = today
    }

    suspend fun setJournalEntry(shabadId: Long, text: String) = context.dataStore.edit { prefs ->
        val raw = prefs[keyJournal] ?: "{}"
        val obj = runCatching { JSONObject(raw) }.getOrDefault(JSONObject())
        if (text.isBlank()) obj.remove(shabadId.toString()) else obj.put(shabadId.toString(), text)
        prefs[keyJournal] = obj.toString()
    }

    suspend fun setLastUpdateCheck(ms: Long) =
        context.dataStore.edit { it[keyLastUpdateCheck] = ms }

    suspend fun replaceFavorites(ids: Set<Long>) =
        context.dataStore.edit { it[keyFavorites] = ids.map { it.toString() }.toSet() }

    suspend fun replaceJournal(map: Map<Long, String>) = context.dataStore.edit { prefs ->
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k.toString(), v) }
        prefs[keyJournal] = obj.toString()
    }

    private fun Preferences.historyList(): List<Long> {
        val raw = this[keyHistoryOrdered] ?: emptySet()
        return raw.mapNotNull { entry ->
            val parts = entry.split("|", limit = 2)
            if (parts.size == 2) {
                val idx = parts[0].toIntOrNull() ?: return@mapNotNull null
                val id = parts[1].toLongOrNull() ?: return@mapNotNull null
                idx to id
            } else null
        }.sortedBy { it.first }.map { it.second }
    }

    private fun todayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Calendar.getInstance().time)

    private fun yesterdayKey(): String {
        val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        return SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(c.time)
    }

    companion object {
        const val MAX_HISTORY = 30
    }
}
