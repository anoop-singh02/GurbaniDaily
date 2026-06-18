package com.anoop.gurbanidaily.data

import java.util.Calendar

object ShabadPicker {

    fun shabadForToday(): Shabad {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val index = dayOfYear % GurbaniData.shabads.size
        return GurbaniData.shabads[index]
    }

    fun randomShabad(): Shabad = GurbaniData.shabads.random()

    fun byId(id: String): Shabad? = GurbaniData.shabads.firstOrNull { it.id == id }

    fun search(query: String): List<Shabad> {
        if (query.isBlank()) return emptyList()
        val q = query.trim().lowercase()
        return GurbaniData.shabads.filter {
            it.gurmukhi.lowercase().contains(q) ||
                it.transliteration.lowercase().contains(q) ||
                it.meaning.lowercase().contains(q) ||
                it.source.lowercase().contains(q) ||
                it.tags.any { tag -> tag.lowercase().contains(q) }
        }
    }
}
