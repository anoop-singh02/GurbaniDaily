package com.anoop.gurbanidaily.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object Backup {

    suspend fun exportTo(context: Context, uri: Uri, prefs: UserPrefs): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val obj = JSONObject().apply {
                    put("version", 2)
                    put("favorites", JSONArray(prefs.favorites.first().toList()))
                    put("history", JSONArray(prefs.history.first()))
                    val journal = JSONObject()
                    prefs.journal.first().forEach { (k, v) -> journal.put(k.toString(), v) }
                    put("journal", journal)
                }
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(obj.toString(2).toByteArray())
                } ?: error("Couldn't open output stream")
            }
        }

    suspend fun importFrom(context: Context, uri: Uri, prefs: UserPrefs): Result<ImportStats> =
        withContext(Dispatchers.IO) {
            runCatching {
                val raw = context.contentResolver.openInputStream(uri)?.use {
                    it.bufferedReader().readText()
                } ?: error("Couldn't open input stream")
                val obj = JSONObject(raw)
                val favs = mutableSetOf<Long>()
                obj.optJSONArray("favorites")?.let {
                    for (i in 0 until it.length()) {
                        val v = it.optLong(i, -1L)
                        if (v > 0) favs.add(v)
                    }
                }
                prefs.replaceFavorites(favs)

                val journalMap = mutableMapOf<Long, String>()
                obj.optJSONObject("journal")?.let { j ->
                    j.keys().forEach { k ->
                        val id = k.toLongOrNull() ?: return@forEach
                        journalMap[id] = j.optString(k, "")
                    }
                }
                prefs.replaceJournal(journalMap)

                ImportStats(favs.size, journalMap.size)
            }
        }

    data class ImportStats(val favorites: Int, val journalEntries: Int)
}
