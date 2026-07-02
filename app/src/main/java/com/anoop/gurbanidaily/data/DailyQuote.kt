package com.anoop.gurbanidaily.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Fetches a fresh shabad each calendar day. Never repeats: keeps a set of
 * previously-shown shabad IDs in filesDir and re-rolls a random pick if it's
 * already been seen.
 */
object DailyQuote {

    private const val CACHE_FILE = "daily_quote.json"
    private const val SEEN_FILE = "daily_quote_seen.json"
    private const val MAX_ROLLS = 12

    suspend fun getForToday(context: Context): Result<OnlineShabad> = withContext(Dispatchers.IO) {
        val today = todayKey()
        val cached = readCache(context)
        if (cached != null && cached.first == today) {
            return@withContext Result.success(cached.second)
        }
        pickNew(context, today)
    }

    /** Force-fetch a new random shabad even if today's cache exists. */
    suspend fun forceNew(context: Context): Result<OnlineShabad> = withContext(Dispatchers.IO) {
        pickNew(context, todayKey())
    }

    private suspend fun pickNew(context: Context, todayKey: String): Result<OnlineShabad> {
        val seen = readSeen(context).toMutableSet()
        var lastFailure: Throwable? = null
        for (attempt in 1..MAX_ROLLS) {
            val result = ShabadApi.fetchRandom()
            result.onSuccess { s ->
                if (s.shabadId !in seen || attempt == MAX_ROLLS) {
                    seen.add(s.shabadId)
                    writeSeen(context, seen)
                    writeCache(context, todayKey, s)
                    // Cache preview for widget / notifications
                    PreviewCache.save(context, SavedShabadPreview.from(s))
                    return Result.success(s)
                }
            }
            result.onFailure { lastFailure = it }
        }
        return Result.failure(lastFailure ?: IllegalStateException("Could not fetch a new shabad"))
    }

    /** Returns cached shabad regardless of date (used by widget / notifications on failure). */
    fun readCachedShabad(context: Context): OnlineShabad? = readCache(context)?.second

    private fun readCache(context: Context): Pair<String, OnlineShabad>? {
        val f = File(context.filesDir, CACHE_FILE)
        if (!f.exists()) return null
        return runCatching {
            val obj = JSONObject(f.readText())
            val date = obj.optString("date")
            val body = obj.optJSONObject("shabad") ?: return null
            val versesArr = body.optJSONArray("verses") ?: JSONArray()
            val verses = mutableListOf<OnlineVerse>()
            for (i in 0 until versesArr.length()) {
                val v = versesArr.getJSONObject(i)
                verses.add(
                    OnlineVerse(
                        gurmukhi = v.optString("g", ""),
                        englishMeaning = v.optString("e", "")
                    )
                )
            }
            date to OnlineShabad(
                shabadId = body.optLong("id"),
                ang = body.optInt("ang"),
                raagEnglish = body.optString("raag", ""),
                writerEnglish = body.optString("writer", ""),
                verses = verses
            )
        }.getOrNull()
    }

    private fun writeCache(context: Context, date: String, shabad: OnlineShabad) {
        val body = JSONObject().apply {
            put("id", shabad.shabadId)
            put("ang", shabad.ang)
            put("raag", shabad.raagEnglish)
            put("writer", shabad.writerEnglish)
            val vs = JSONArray()
            shabad.verses.forEach { v ->
                vs.put(JSONObject().apply {
                    put("g", v.gurmukhi)
                    put("e", v.englishMeaning)
                })
            }
            put("verses", vs)
        }
        val obj = JSONObject().apply {
            put("date", date)
            put("shabad", body)
        }
        File(context.filesDir, CACHE_FILE).writeText(obj.toString())
    }

    private fun readSeen(context: Context): Set<Long> {
        val f = File(context.filesDir, SEEN_FILE)
        if (!f.exists()) return emptySet()
        return runCatching {
            val arr = JSONArray(f.readText())
            (0 until arr.length()).map { arr.optLong(it) }.filter { it > 0 }.toSet()
        }.getOrDefault(emptySet())
    }

    private fun writeSeen(context: Context, ids: Set<Long>) {
        val arr = JSONArray()
        ids.forEach { arr.put(it) }
        File(context.filesDir, SEEN_FILE).writeText(arr.toString())
    }

    private fun todayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Calendar.getInstance().time)
}
