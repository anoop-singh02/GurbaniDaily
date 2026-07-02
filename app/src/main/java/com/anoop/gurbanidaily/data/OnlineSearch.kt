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
    val gurmukhi: String,        // Unicode ਗੁਰਮੁਖੀ
    val englishMeaning: String,
    val source: String
)

object OnlineSearch {

    suspend fun searchEnglish(query: String): Result<List<OnlineSearchResult>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val q = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.toString())
                val url = URL("https://api.banidb.com/v2/search/$q/3?source=G")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8_000
                    readTimeout = 15_000
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "GurbaniDaily/4.0")
                }
                parse(conn.inputStream.bufferedReader().use { it.readText() })
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
            val gurmukhi = firstNonBlank(
                verse?.optString("unicode", ""),
                v.optString("unicode", ""),
                verse?.optString("gurmukhi", "")
            )
            val englishMeaning = v.optJSONObject("translation")
                ?.optJSONObject("en")?.optString("bdb", "").orEmpty()
            val writer = v.optJSONObject("writer")?.optString("english", "").orEmpty()
            val raag = v.optJSONObject("raag")?.optString("english", "").orEmpty()
            val ang = v.optInt("pageNo", 0)
            val parts = listOfNotNull(
                writer.takeIf { it.isNotBlank() },
                raag.takeIf { it.isNotBlank() }?.let { formatRaag(it) },
                if (ang > 0) "Ang $ang" else null
            )
            if (gurmukhi.isNotBlank()) {
                out.add(
                    OnlineSearchResult(
                        shabadId = shabadId,
                        gurmukhi = gurmukhi,
                        englishMeaning = englishMeaning,
                        source = parts.joinToString(" · ")
                    )
                )
            }
        }
        return out
    }

    private fun firstNonBlank(vararg values: String?): String =
        values.firstOrNull { !it.isNullOrBlank() }?.trim().orEmpty()
}
