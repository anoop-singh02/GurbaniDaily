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
    val gurmukhi: String,
    val transliteration: String,
    val englishMeaning: String,
    val punjabiMeaning: String
)

data class Hukamnama(
    val dateLabel: String,
    val ang: String,
    val raag: String,
    val writer: String,
    val verses: List<HukamnamaVerse>
) {
    val combinedGurmukhi: String get() = verses.joinToString("\n") { it.gurmukhi }
    val combinedTransliteration: String
        get() = verses.joinToString("\n") { it.transliteration }
    val combinedMeaning: String get() = verses.joinToString("\n") { it.englishMeaning }
}

object HukamnamaRepo {

    private const val TODAY_ENDPOINT = "https://api.banidb.com/v2/hukamnamas/today"
    private fun shabadEndpoint(id: Long) = "https://api.banidb.com/v2/shabads/$id"
    private const val CACHE_FILE = "hukamnama_today.json"

    suspend fun fetchToday(context: Context): Result<Hukamnama> = withContext(Dispatchers.IO) {
        runCatching {
            val todayRaw = httpGet(TODAY_ENDPOINT)
            val (shabadIds, dateLabel) = parseTodayResponse(todayRaw)
            val firstId = shabadIds.firstOrNull()
                ?: error("No shabad IDs returned by BaniDB today endpoint")
            val shabadRaw = httpGet(shabadEndpoint(firstId))
            val hukamnama = parseShabadResponse(shabadRaw, dateLabel)
            File(context.filesDir, CACHE_FILE).writeText(shabadRaw + "|||DATE|||" + dateLabel)
            hukamnama
        }.recoverCatching {
            val cached = cachedRaw(context) ?: throw it
            val (raw, date) = cached
            parseShabadResponse(raw, date)
        }
    }

    suspend fun loadCachedOrFetch(context: Context): Result<Hukamnama> = withContext(Dispatchers.IO) {
        val cached = cachedRaw(context)
        if (cached != null) {
            val parsed = runCatching { parseShabadResponse(cached.first, cached.second) }
            if (parsed.isSuccess) return@withContext parsed
        }
        fetchToday(context)
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

    private fun httpGet(url: String): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 12_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "GurbaniDaily/3.0")
        }
        return conn.inputStream.bufferedReader().use { it.readText() }
    }

    private fun parseTodayResponse(raw: String): Pair<List<Long>, String> {
        val root = JSONObject(raw)
        val ids = mutableListOf<Long>()
        // BaniDB has been known to return either shabadIds [] or shabads: [{shabadId: ...}]
        val shabadIds = root.optJSONArray("shabadIds")
        if (shabadIds != null) {
            for (i in 0 until shabadIds.length()) ids.add(shabadIds.getLong(i))
        }
        val shabads = root.optJSONArray("shabads")
        if (shabads != null) {
            for (i in 0 until shabads.length()) {
                val obj = shabads.getJSONObject(i)
                val id = obj.optLong("shabadId", 0L)
                if (id > 0L) ids.add(id)
            }
        }

        val dateLabel = root.optJSONObject("date")
            ?.optJSONObject("gregorian")
            ?.optString("date")
            ?.let { formatDate(it) }
            ?: formatToday()
        return ids.distinct() to dateLabel
    }

    private fun parseShabadResponse(raw: String, dateLabel: String): Hukamnama {
        val root = JSONObject(raw)
        val info = root.optJSONObject("shabadInfo") ?: JSONObject()
        val angRaw = info.optInt("pageNo", 0)
        val ang = if (angRaw > 0) "Ang $angRaw" else ""
        val raag = info.optJSONObject("raag")?.optString("english")
            ?: info.optJSONObject("raag")?.optString("raagEnglish", "")
            ?: ""
        val writer = info.optJSONObject("writer")?.optString("english", "") ?: ""

        val versesArr = root.optJSONArray("verses") ?: JSONArray()
        val verses = mutableListOf<HukamnamaVerse>()
        for (i in 0 until versesArr.length()) {
            val v = versesArr.getJSONObject(i)
            val verseObj = v.optJSONObject("verse") ?: v
            val gurmukhi = verseObj.optString("gurmukhi", "")
                .ifBlank { verseObj.optString("unicode", "") }
            if (gurmukhi.isBlank()) continue

            val translit = v.optJSONObject("transliteration")?.optString("english", "")
                ?: verseObj.optJSONObject("transliteration")?.optString("english", "")
                ?: ""

            val translation = v.optJSONObject("translation")
                ?: verseObj.optJSONObject("translation")

            val englishMeaning = translation?.optJSONObject("en")?.optString("bdb", "")
                ?: translation?.optJSONObject("english")?.optString("ssk", "")
                ?: ""

            val punjabiMeaning = translation?.optJSONObject("pu")
                ?.optJSONObject("bdb")?.optString("unicode", "")
                ?: ""

            verses.add(
                HukamnamaVerse(
                    gurmukhi = gurmukhi,
                    transliteration = translit,
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

    private fun formatDate(raw: String): String {
        if (raw.isBlank()) return formatToday()
        return try {
            val ins = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val out = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH)
            out.format(ins.parse(raw) ?: Calendar.getInstance().time)
        } catch (_: Exception) {
            raw
        }
    }

    private fun formatToday(): String =
        SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH).format(Calendar.getInstance().time)
}
