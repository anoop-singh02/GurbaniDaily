package com.anoop.gurbanidaily.data

data class Shabad(
    val id: String,
    val gurmukhi: String,
    val transliteration: String,
    val meaning: String,
    val source: String,
    val tags: List<String> = emptyList()
)
