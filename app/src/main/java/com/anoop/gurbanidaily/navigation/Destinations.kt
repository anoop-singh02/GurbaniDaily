package com.anoop.gurbanidaily.navigation

sealed class Dest(val route: String) {
    data object Main : Dest("main")
    data object Favorites : Dest("favorites")
    data object History : Dest("history")
    data object Search : Dest("search")
    data object Settings : Dest("settings")
    data object Raags : Dest("raags")
    data object Category : Dest("category/{id}") {
        fun build(id: String) = "category/$id"
    }
    data object Reader : Dest("reader/{id}") {
        fun build(id: String) = "reader/$id"
    }
    data object AngBrowse : Dest("angs?start={start}&end={end}&raag={raag}") {
        fun build(start: Int, end: Int, raag: String): String {
            val safeRaag = java.net.URLEncoder.encode(raag.ifBlank { "Browse" }, "UTF-8")
            return "angs?start=$start&end=$end&raag=$safeRaag"
        }
    }
}
