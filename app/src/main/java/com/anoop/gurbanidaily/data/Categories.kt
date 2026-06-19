package com.anoop.gurbanidaily.data

data class Category(
    val id: String,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val tagMatch: Set<String>,
    val sourceContains: List<String> = emptyList()
)

object Categories {
    val all: List<Category> = listOf(
        Category(
            id = "nitnem",
            title = "Nitnem",
            subtitle = "Daily banis",
            emoji = "☀️",
            tagMatch = setOf("nitnem", "japji", "anand", "rehras", "sohila")
        ),
        Category(
            id = "sukhmani",
            title = "Sukhmani Sahib",
            subtitle = "Jewel of Peace",
            emoji = "📿",
            tagMatch = setOf("sukhmani")
        ),
        Category(
            id = "saloks",
            title = "Saloks",
            subtitle = "Tegh Bahadur · Kabir · Fareed",
            emoji = "📜",
            tagMatch = setOf("salok", "kabir"),
            sourceContains = listOf("Tegh Bahadur", "Kabir", "Fareed")
        ),
        Category(
            id = "wisdom",
            title = "Daily Wisdom",
            subtitle = "Curated reflections",
            emoji = "✨",
            tagMatch = emptySet()
        )
    )

    fun shabadsIn(category: Category): List<Shabad> {
        if (category.id == "wisdom") {
            // Wisdom = everything not matched by other categories
            val claimed = all.filter { it.id != "wisdom" }
                .flatMap { c -> shabadsInRaw(c).map { it.id } }
                .toSet()
            return GurbaniData.shabads.filter { it.id !in claimed }
        }
        return shabadsInRaw(category)
    }

    private fun shabadsInRaw(category: Category): List<Shabad> {
        return GurbaniData.shabads.filter { s ->
            val tagHit = s.tags.any { it in category.tagMatch }
            val sourceHit = category.sourceContains.any { s.source.contains(it) }
            tagHit || sourceHit
        }
    }

    fun byId(id: String): Category? = all.firstOrNull { it.id == id }
}
