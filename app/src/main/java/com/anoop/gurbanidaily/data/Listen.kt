package com.anoop.gurbanidaily.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object Listen {

    fun openYouTube(context: Context, shabad: Shabad) {
        val firstLine = shabad.gurmukhi.lineSequence().first().trim()
        val cleanSource = shabad.source
            .substringBefore("—")
            .substringBefore(",")
            .trim()
        val query = listOfNotNull(
            firstLine.takeIf { it.isNotEmpty() },
            cleanSource.takeIf { it.isNotEmpty() },
            "kirtan"
        ).joinToString(" ")

        val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        val uri = Uri.parse("https://www.youtube.com/results?search_query=$encoded")

        val viewIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(viewIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "No browser found to play kirtan", Toast.LENGTH_SHORT).show()
        }
    }
}
