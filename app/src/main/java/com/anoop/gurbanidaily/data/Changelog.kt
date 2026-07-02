package com.anoop.gurbanidaily.data

import android.content.Context

object Changelog {

    /** Full changelog as bundled in the APK's assets. */
    fun readAll(context: Context): String = runCatching {
        context.assets.open("CHANGELOG.md").bufferedReader().use { it.readText() }
    }.getOrElse { "No changelog bundled." }

    /** Just the topmost "## …" section (the current release notes). */
    fun latestSection(context: Context): String {
        val full = readAll(context)
        val lines = full.lineSequence().toList()
        val start = lines.indexOfFirst { it.startsWith("## ") }
        if (start < 0) return full
        val end = lines.drop(start + 1).indexOfFirst { it.startsWith("## ") }
            .let { if (it < 0) lines.size else start + 1 + it }
        return lines.subList(start, end).joinToString("\n").trim()
    }
}
