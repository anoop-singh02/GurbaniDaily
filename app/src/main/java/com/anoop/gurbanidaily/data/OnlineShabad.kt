package com.anoop.gurbanidaily.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** A verse from Sri Guru Granth Sahib Ji. Gurmukhi is Unicode ਗੁਰਮੁਖੀ, not AnmolLipi ASCII. */
data class OnlineVerse(
    val gurmukhi: String,
    val englishMeaning: String
)

data class OnlineShabad(
    val shabadId: Long,
    val ang: Int,
    val raagEnglish: String,
    val writerEnglish: String,
    val verses: List<OnlineVerse>
) {
    val allGurmukhi: String get() = verses.joinToString("\n") { it.gurmukhi }
    val allEnglish: String get() = verses.joinToString("\n") { it.englishMeaning }
    val firstGurmukhi: String get() = verses.firstOrNull()?.gurmukhi.orEmpty()
    val sourceLabel: String
        get() = listOfNotNull(
            writerEnglish.takeIf { it.isNotBlank() },
            raagEnglish.takeIf { it.isNotBlank() }?.let { "Raag $it" },
            if (ang > 0) "Ang $ang" else null
        ).joinToString(" · ")
}

object ShabadApi {

    private const val UA = "GurbaniDaily/4.0"

    suspend fun fetchById(id: Long): Result<OnlineShabad> = withContext(Dispatchers.IO) {
        runCatching {
            val raw = httpGet("https://api.banidb.com/v2/shabads/$id")
            parseShabad(raw, id)
        }
    }

    /** Return a random shabad; endpoint is used by BaniDB SDK app. */
    suspend fun fetchRandom(): Result<OnlineShabad> = withContext(Dispatchers.IO) {
        runCatching {
            val raw = httpGet("https://api.banidb.com/v2/random?source=G")
            val id = JSONObject(raw).optJSONObject("shabadInfo")?.optLong("shabadId", -1L) ?: -1L
            if (id > 0) {
                parseShabad(raw, id)
            } else {
                // Fallback: pick a random ang, then fetch its first shabad
                val ang = (1..1430).random()
                val angRaw = httpGet("https://api.banidb.com/v2/angs/$ang?source=G")
                val page = JSONObject(angRaw).optJSONArray("page") ?: error("Empty ang $ang")
                val ids = mutableListOf<Long>()
                for (i in 0 until page.length()) {
                    val sid = page.getJSONObject(i).optLong("shabadId", -1L)
                    if (sid > 0 && sid !in ids) ids.add(sid)
                }
                val pick = ids.randomOrNull() ?: error("No shabads on ang $ang")
                parseShabad(httpGet("https://api.banidb.com/v2/shabads/$pick"), pick)
            }
        }
    }

    private fun parseShabad(raw: String, fallbackId: Long): OnlineShabad {
        val root = JSONObject(raw)
        val info = root.optJSONObject("shabadInfo") ?: JSONObject()
        val id = info.optLong("shabadId", fallbackId)
        val ang = info.optInt("pageNo", 0)
        val raag = info.optJSONObject("raag")?.optString("english", "").orEmpty()
        val writer = info.optJSONObject("writer")?.optString("english", "").orEmpty()

        val versesArr = root.optJSONArray("verses") ?: org.json.JSONArray()
        val verses = mutableListOf<OnlineVerse>()
        for (i in 0 until versesArr.length()) {
            val v = versesArr.getJSONObject(i)
            val verse = v.optJSONObject("verse") ?: v
            // Prefer Unicode Gurmukhi. Fall back to gurmukhi/akhar/padched.
            val gurmukhi = firstNonBlank(
                verse.optString("unicode", ""),
                v.optString("unicode", ""),
                verse.optJSONObject("gurmukhi")?.optString("unicode", ""),
                verse.optString("gurmukhi", ""),
                verse.optString("akhar", "")
            )
            if (gurmukhi.isBlank()) continue

            val translation = v.optJSONObject("translation") ?: verse.optJSONObject("translation")
            val english = firstNonBlank(
                translation?.optJSONObject("en")?.optString("bdb", ""),
                translation?.optJSONObject("english")?.optString("ssk", ""),
                translation?.optJSONObject("english")?.optString("bdb", "")
            )

            verses.add(OnlineVerse(gurmukhi = gurmukhi, englishMeaning = english))
        }

        return OnlineShabad(
            shabadId = id,
            ang = ang,
            raagEnglish = raag,
            writerEnglish = writer,
            verses = verses
        )
    }

    private fun firstNonBlank(vararg values: String?): String =
        values.firstOrNull { !it.isNullOrBlank() }?.trim().orEmpty()

    private fun httpGet(url: String): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", UA)
        }
        return conn.inputStream.bufferedReader().use { it.readText() }
    }
}

/** Per-shabad preview cache, so favourites/history display without re-fetching. */
data class SavedShabadPreview(
    val id: Long,
    val firstGurmukhi: String,
    val englishSnippet: String,
    val source: String
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("g", firstGurmukhi)
        put("e", englishSnippet)
        put("s", source)
    }

    companion object {
        fun from(shabad: OnlineShabad): SavedShabadPreview = SavedShabadPreview(
            id = shabad.shabadId,
            firstGurmukhi = shabad.firstGurmukhi,
            englishSnippet = shabad.allEnglish.take(200),
            source = shabad.sourceLabel
        )
        fun fromJson(obj: JSONObject) = SavedShabadPreview(
            id = obj.optLong("id"),
            firstGurmukhi = obj.optString("g"),
            englishSnippet = obj.optString("e"),
            source = obj.optString("s")
        )
    }
}

object PreviewCache {
    private const val FILE = "shabad_previews.json"

    fun read(context: Context): Map<Long, SavedShabadPreview> {
        val f = File(context.filesDir, FILE)
        if (!f.exists()) return emptyMap()
        return runCatching {
            val obj = JSONObject(f.readText())
            val out = mutableMapOf<Long, SavedShabadPreview>()
            obj.keys().forEach { k ->
                val id = k.toLongOrNull() ?: return@forEach
                out[id] = SavedShabadPreview.fromJson(obj.getJSONObject(k))
            }
            out
        }.getOrDefault(emptyMap())
    }

    fun save(context: Context, preview: SavedShabadPreview) {
        val map = read(context).toMutableMap()
        map[preview.id] = preview
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k.toString(), v.toJson()) }
        File(context.filesDir, FILE).writeText(obj.toString())
    }
}
