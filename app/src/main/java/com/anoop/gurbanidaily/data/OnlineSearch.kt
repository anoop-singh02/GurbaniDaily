package com.anoop.gurbanidaily.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class OnlineSearchResult(
    val shabadId: Long,
    val gurmukhi: String,
    val englishMeaning: String,
    val source: String
)

object OnlineSearch {

    // BaniDB search type 3 = English-text search across SGGSJ
    suspend fun searchEnglish(query: String): Result<List<OnlineSearchResult>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val q = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.toString())
                val url = URL("https://api.banidb.com/v2/search/$q/3?source=G")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8_000
                    readTimeout = 12_000
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "GurbaniDaily/3.0")
                }
                val raw = conn.inputStream.bufferedReader().use { it.readText() }
                parse(raw)
            }
        }

    private fun parse(raw: String): List<OnlineSearchResult> {
        val root = JSONObject(raw)
        val verses = root.optJSONArray("verses") ?: return emptyList()
        val out = mutableListOf<OnlineSearchResult>()
        for (i in 0 until verses.length()) {
            val v = verses.getJSONObject(i)
            val shabadId = v.optLong("shabadId", -1L)
            if (shabadId <= 0L) continue
            val verse = v.optJSONObject("verse")
            val gurmukhi = verse?.optString("gurmukhi", "") ?: ""
            val englishMeaning = v.optJSONObject("translation")
                ?.optJSONObject("en")?.optString("bdb", "") ?: ""
            val writer = v.optJSONObject("writer")?.optString("english", "") ?: ""
            val raag = v.optJSONObject("raag")?.optString("english", "") ?: ""
            val ang = v.optInt("pageNo", 0)
            val sourceParts = listOfNotNull(
                writer.takeIf { it.isNotBlank() },
                raag.takeIf { it.isNotBlank() }?.let { "Raag $it" },
                if (ang > 0) "Ang $ang" else null
            )
            out.add(
                OnlineSearchResult(
                    shabadId = shabadId,
                    gurmukhi = gurmukhi,
                    englishMeaning = englishMeaning,
                    source = sourceParts.joinToString(" · ")
                )
            )
        }
        return out
    }
}
