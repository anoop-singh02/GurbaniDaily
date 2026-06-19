package com.anoop.gurbanidaily.navigation

sealed class Dest(val route: String) {
    data object Main : Dest("main")
    data object Favorites : Dest("favorites")
    data object History : Dest("history")
    data object Search : Dest("search")
    data object Settings : Dest("settings")
    data object Category : Dest("category/{id}") {
        fun build(id: String) = "category/$id"
    }
}
