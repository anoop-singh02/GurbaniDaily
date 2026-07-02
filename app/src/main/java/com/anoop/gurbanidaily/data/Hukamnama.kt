package com.anoop.gurbanidaily.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class HukamnamaVerse(
    val gurmukhi: String,           // Unicode ਗੁਰਮੁਖੀ
    val englishMeaning: String,
    val punjabiMeaning: String      // Punjabi translation in Unicode
)

data class Hukamnama(
    val dateLabel: String,
    val ang: String,
    val raag: String,
    val writer: String,
    val verses: List<HukamnamaVerse>
) {
    val combinedGurmukhi: String get() = verses.joinToString("\n") { it.gurmukhi }
    val combinedMeaning: String get() = verses.joinToString("\n") { it.englishMeaning }
    val combinedPunjabi: String get() = verses.joinToString("\n") { it.punjabiMeaning }
}

object HukamnamaRepo {

    private const val TODAY_ENDPOINT = "https://api.banidb.com/v2/hukamnamas/today"
    private fun dateEndpoint(date: String) = "https://api.banidb.com/v2/hukamnamas/$date"
    private fun shabadEndpoint(id: Long) = "https://api.banidb.com/v2/shabads/$id"
    private const val CACHE_FILE = "hukamnama_today.json"

    suspend fun fetchToday(context: Context): Result<Hukamnama> = withContext(Dispatchers.IO) {
        runCatching {
            val todayRaw = httpGet(TODAY_ENDPOINT)
            val (shabadIds, dateLabel) = parseTodayResponse(todayRaw)
            val firstId = shabadIds.firstOrNull() ?: error("No Hukamnama returned")
            val shabadRaw = httpGet(shabadEndpoint(firstId))
            val hukamnama = parseShabadResponse(shabadRaw, dateLabel)
            File(context.filesDir, CACHE_FILE).writeText(shabadRaw + "|||DATE|||" + dateLabel)
            hukamnama
        }.recoverCatching {
            val cached = cachedRaw(context) ?: throw it
            parseShabadResponse(cached.first, cached.second)
        }
    }

    suspend fun loadCachedOrFetch(context: Context): Result<Hukamnama> = withContext(Dispatchers.IO) {
        val cached = cachedRaw(context)
        // Only serve the cache if it's for today. Otherwise re-fetch so the
        // Hukamnama tab always shows today's Hukamnama from Sri Harmandir Sahib.
        if (cached != null && cached.second == formatToday()) {
            val parsed = runCatching { parseShabadResponse(cached.first, cached.second) }
            if (parsed.isSuccess) return@withContext parsed
        }
        fetchToday(context)
    }

    suspend fun fetchShabadById(id: Long): Result<Hukamnama> = withContext(Dispatchers.IO) {
        runCatching { parseShabadResponse(httpGet(shabadEndpoint(id)), "") }
    }

    /** yyyy-MM-dd */
    suspend fun fetchForDate(date: String): Result<Hukamnama> = withContext(Dispatchers.IO) {
        runCatching {
            val raw = httpGet(dateEndpoint(date))
            val (shabadIds, label) = parseTodayResponse(raw)
            val id = shabadIds.firstOrNull() ?: error("No Hukamnama for $date")
            val shabadRaw = httpGet(shabadEndpoint(id))
            parseShabadResponse(shabadRaw, label.ifBlank { formatDate(date) })
        }
    }

    private fun cachedRaw(context: Context): Pair<String, String>? {
        val f = File(context.filesDir, CACHE_FILE)
        if (!f.exists()) return null
        val content = f.readText()
        val parts = content.split("|||DATE|||", limit = 2)
        val raw = parts.getOrNull(0) ?: return null
        val date = parts.getOrNull(1) ?: formatToday()
        return raw to date
    }

    private fun parseTodayResponse(raw: String): Pair<List<Long>, String> {
        val root = JSONObject(raw)
        val ids = mutableListOf<Long>()
        root.optJSONArray("shabadIds")?.let {
            for (i in 0 until it.length()) ids.add(it.getLong(i))
        }
        root.optJSONArray("shabads")?.let {
            for (i in 0 until it.length()) {
                val id = it.getJSONObject(i).optLong("shabadId", 0)
                if (id > 0) ids.add(id)
            }
        }
        val dateLabel = root.optJSONObject("date")
            ?.optJSONObject("gregorian")?.optString("date")
            ?.let { formatDate(it) }
            ?: formatToday()
        return ids.distinct() to dateLabel
    }

    private fun parseShabadResponse(raw: String, dateLabel: String): Hukamnama {
        val root = JSONObject(raw)
        val info = root.optJSONObject("shabadInfo") ?: JSONObject()
        val angRaw = info.optInt("pageNo", 0)
        val ang = if (angRaw > 0) "Ang $angRaw" else ""
        val raag = info.optJSONObject("raag")?.optString("english", "").orEmpty()
        val writer = info.optJSONObject("writer")?.optString("english", "").orEmpty()

        val versesArr = root.optJSONArray("verses") ?: JSONArray()
        val verses = mutableListOf<HukamnamaVerse>()
        for (i in 0 until versesArr.length()) {
            val v = versesArr.getJSONObject(i)
            val verse = v.optJSONObject("verse") ?: v
            // Prefer Unicode Gurmukhi over the AnmolLipi ASCII "gurmukhi" field.
            val gurmukhi = firstNonBlank(
                verse.optString("unicode", ""),
                v.optString("unicode", ""),
                verse.optJSONObject("gurmukhi")?.optString("unicode", ""),
                verse.optString("gurmukhi", "")
            )
            if (gurmukhi.isBlank()) continue

            val translation = v.optJSONObject("translation") ?: verse.optJSONObject("translation")
            val englishMeaning = firstNonBlank(
                translation?.optJSONObject("en")?.optString("bdb", ""),
                translation?.optJSONObject("english")?.optString("ssk", ""),
                translation?.optJSONObject("english")?.optString("bdb", "")
            )
            val punjabiMeaning = firstNonBlank(
                translation?.optJSONObject("pu")?.optJSONObject("bdb")?.optString("unicode", ""),
                translation?.optJSONObject("pu")?.optJSONObject("ss")?.optString("unicode", ""),
                translation?.optJSONObject("punjabi")?.optString("bdb", "")
            )

            verses.add(
                HukamnamaVerse(
                    gurmukhi = gurmukhi,
                    englishMeaning = englishMeaning,
                    punjabiMeaning = punjabiMeaning
                )
            )
        }

        return Hukamnama(
            dateLabel = dateLabel,
            ang = ang,
            raag = raag,
            writer = writer,
            verses = verses
        )
    }

    private fun firstNonBlank(vararg values: String?): String =
        values.firstOrNull { !it.isNullOrBlank() }?.trim().orEmpty()

    private fun formatDate(raw: String): String {
        if (raw.isBlank()) return formatToday()
        return try {
            val ins = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val out = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH)
            out.format(ins.parse(raw) ?: Calendar.getInstance().time)
        } catch (_: Exception) { raw }
    }

    private fun formatToday(): String =
        SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH).format(Calendar.getInstance().time)

    private fun httpGet(url: String): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "GurbaniDaily/4.0")
        }
        return conn.inputStream.bufferedReader().use { it.readText() }
    }
}
