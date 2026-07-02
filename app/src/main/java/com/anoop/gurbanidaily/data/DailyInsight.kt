package com.anoop.gurbanidaily.data

import java.util.Locale

data class DailyInsight(
    val gurmukhiLine: String,
    val meaning: String,
    val reflection: String
)

object DailyInsightBuilder {
    fun from(shabad: OnlineShabad): DailyInsight {
        val verse = shabad.verses.firstOrNull { it.englishMeaning.isNotBlank() }
            ?: shabad.verses.firstOrNull()
        val meaning = shortMeaning(shabad)
        return DailyInsight(
            gurmukhiLine = verse?.gurmukhi.orEmpty(),
            meaning = meaning,
            reflection = reflectionFor(meaning.ifBlank { shabad.allEnglish })
        )
    }

    private fun shortMeaning(shabad: OnlineShabad): String {
        val text = shabad.verses
            .map { it.englishMeaning }
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .cleanText()
        if (text.isBlank()) return "Read this line slowly and sit with its message for a moment."

        val sentences = text.split(Regex("""(?<=[.!?])\s+"""))
            .map { it.trim() }
            .filter { it.isNotBlank() }
        val picked = sentences.take(2).joinToString(" ").ifBlank { text }
        return picked.takeWordSafe(300)
    }

    private fun reflectionFor(text: String): String {
        val lower = text.lowercase(Locale.ENGLISH)
        return when {
            lower.hasAny("naam", "name of the lord", "lord", "god", "creator", "har") ->
                "Let remembrance steady you today. Before the day pulls you around, pause and bring your attention back to the Guru."
            lower.hasAny("fear", "afraid", "worry", "anxiety", "suffering", "pain") ->
                "Let this remind you that fear does not have to lead you. Breathe, remember the Guru, and take the next honest step."
            lower.hasAny("ego", "pride", "self-conceit", "selfish") ->
                "Let this soften the ego today. Speak gently, listen first, and choose humility in one real moment."
            lower.hasAny("serve", "service", "seva", "servant") ->
                "Turn this understanding into seva today. Help someone quietly, without needing credit."
            lower.hasAny("truth", "true", "honest", "falsehood") ->
                "Keep your day close to truth. Let one decision be guided by honesty instead of pressure."
            lower.hasAny("love", "beloved", "mercy", "compassion") ->
                "Let love for the Guru shape how you move today. Be patient with yourself and kind with others."
            else ->
                "Carry this Gurbani slowly today. Let it bring your mind back to the Guru, then meet the day with calm, honest action."
        }
    }

    private fun String.cleanText(): String = replace(Regex("""\s+"""), " ").trim()

    private fun String.takeWordSafe(maxChars: Int): String {
        if (length <= maxChars) return this
        val cut = take(maxChars)
        return cut.substringBeforeLast(" ").trimEnd(',', ';', ':') + "..."
    }

    private fun String.hasAny(vararg needles: String): Boolean =
        needles.any { contains(it) }
}
