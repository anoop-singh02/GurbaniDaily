package com.anoop.gurbanidaily.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class Raag(
    val id: Int,
    val english: String,
    val angStart: Int,
    val angEnd: Int
)

object Raags {

    suspend fun list(): Result<List<Raag>> = withContext(Dispatchers.IO) {
        runCatching {
            val raw = httpGet("https://api.banidb.com/v2/raags")
            val root = parseAsArray(raw)
            val out = mutableListOf<Raag>()
            for (i in 0 until root.length()) {
                val r = root.getJSONObject(i)
                val id = r.optInt("raagId", -1)
                if (id <= 0) continue
                out.add(
                    Raag(
                        id = id,
                        english = r.optString("raagEnglish", ""),
                        angStart = r.optInt("angStart", 0),
                        angEnd = r.optInt("angEnd", 0)
                    )
                )
            }
            out
        }
    }

    private fun parseAsArray(raw: String): JSONArray {
        // BaniDB sometimes wraps the array in an object; handle both.
        val trimmed = raw.trim()
        return if (trimmed.startsWith("[")) JSONArray(trimmed)
        else {
            val obj = JSONObject(trimmed)
            obj.optJSONArray("raags") ?: obj.optJSONArray("results") ?: JSONArray()
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
