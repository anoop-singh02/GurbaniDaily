package com.anoop.gurbanidaily.navigation

sealed class Dest(val route: String) {
    data object Main : Dest("main")
    data object Favorites : Dest("favorites")
    data object History : Dest("history")
    data object Search : Dest("search")
    data object Settings : Dest("settings")
    data object Changelog : Dest("changelog")
    data object PunjabiMonths : Dest("punjabi-months")
    data object Reader : Dest("reader/{id}") {
        fun build(id: String) = "reader/$id"
    }
}
