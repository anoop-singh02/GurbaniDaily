package com.anoop.gurbanidaily.data

import java.util.Calendar

data class NanakshahiMonth(
    val ordinal: Int,          // 1..12
    val english: String,       // e.g. "Chet"
    val gurmukhi: String,      // e.g. "ਚੇਤ"
    val startMonth: Int,       // Gregorian month (1..12)
    val startDay: Int,         // Gregorian day of that month
    val greeting: String       // Short English greeting for the notification body
)

/**
 * Fixed Nanakshahi calendar (2003 revision). Each month begins on a fixed
 * Gregorian date; the first day of the month is the "sangrand".
 */
object NanakshahiCalendar {

    val months: List<NanakshahiMonth> = listOf(
        NanakshahiMonth(1, "Chet", "ਚੇਤ", 3, 14,
            "The new spring of the Nanakshahi year begins. Sangrand of Chet."),
        NanakshahiMonth(2, "Vaisakh", "ਵੈਸਾਖ", 4, 14,
            "The month of Vaisakh — the birth of the Khalsa. Sangrand of Vaisakh."),
        NanakshahiMonth(3, "Jeth", "ਜੇਠ", 5, 15,
            "Sangrand of Jeth — the long, warm days of remembering the Naam."),
        NanakshahiMonth(4, "Harh", "ਹਾੜ", 6, 15,
            "Sangrand of Harh — Guru Arjan Dev Ji taught patience in this heat."),
        NanakshahiMonth(5, "Sawan", "ਸਾਵਣ", 7, 16,
            "Sangrand of Sawan — the monsoon month of longing for the Beloved."),
        NanakshahiMonth(6, "Bhadon", "ਭਾਦੋਂ", 8, 16,
            "Sangrand of Bhadon — Gurbani calls this the season of surrender."),
        NanakshahiMonth(7, "Assu", "ਅੱਸੂ", 9, 15,
            "Sangrand of Assu — turning toward the light of the True Guru."),
        NanakshahiMonth(8, "Katak", "ਕੱਤਕ", 10, 15,
            "Sangrand of Katak — the month of Guru Nanak Dev Ji's birthday."),
        NanakshahiMonth(9, "Maghar", "ਮੱਘਰ", 11, 14,
            "Sangrand of Maghar — Guru Tegh Bahadur Ji's sacrifice remembered."),
        NanakshahiMonth(10, "Poh", "ਪੋਹ", 12, 14,
            "Sangrand of Poh — the cold month tempers the heart."),
        NanakshahiMonth(11, "Magh", "ਮਾਘ", 1, 13,
            "Sangrand of Magh — cleansing month, remembering the Sahibzade."),
        NanakshahiMonth(12, "Phagun", "ਫੱਗਣ", 2, 12,
            "Sangrand of Phagun — the final month, love ripens for the Guru.")
    )

    /** Returns the month that BEGINS today, or null if today is not a sangrand. */
    fun sangrandStartingToday(cal: Calendar = Calendar.getInstance()): NanakshahiMonth? {
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        return months.firstOrNull { it.startMonth == m && it.startDay == d }
    }

    /** Returns the Nanakshahi month currently in progress. */
    fun currentMonth(cal: Calendar = Calendar.getInstance()): NanakshahiMonth {
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        val today = m * 100 + d
        // Search backwards for the most recent sangrand that's <= today. Months
        // are stored in ordinal order; sort by the calendar day they begin on
        // (Chet = 14 March is the "start" of the Nanakshahi year).
        val chronological = months.sortedBy { it.startMonth * 100 + it.startDay }
        return chronological.lastOrNull { (it.startMonth * 100 + it.startDay) <= today }
            ?: months.last() // e.g. Feb 1 → Magh (started Jan 13)
    }
}
