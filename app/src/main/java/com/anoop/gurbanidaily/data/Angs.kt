package com.anoop.gurbanidaily.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class AngShabadEntry(
    val shabadId: Long,
    val firstGurmukhi: String,
    val englishMeaning: String,
    val writer: String
)

object Angs {

    suspend fun fetch(angNo: Int): Result<List<AngShabadEntry>> = withContext(Dispatchers.IO) {
        runCatching {
            val raw = httpGet("https://api.banidb.com/v2/angs/$angNo?source=G")
            val root = JSONObject(raw)
            val page = root.optJSONArray("page") ?: return@runCatching emptyList()
            // Group verses by shabadId, keep first verse per shabad as preview
            val seen = linkedMapOf<Long, AngShabadEntry>()
            for (i in 0 until page.length()) {
                val v = page.getJSONObject(i)
                val id = v.optLong("shabadId", -1L)
                if (id <= 0 || seen.containsKey(id)) continue
                val verse = v.optJSONObject("verse")
                val gurmukhi = verse?.optString("gurmukhi", "") ?: ""
                val english = v.optJSONObject("translation")
                    ?.optJSONObject("en")?.optString("bdb", "") ?: ""
                val writer = v.optJSONObject("writer")?.optString("english", "") ?: ""
                seen[id] = AngShabadEntry(id, gurmukhi, english, writer)
            }
            seen.values.toList()
        }
    }

    private fun httpGet(url: String): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 12_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "GurbaniDaily/3.2")
        }
        return conn.inputStream.bufferedReader().use { it.readText() }
    }
}
