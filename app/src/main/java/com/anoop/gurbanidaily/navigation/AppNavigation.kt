package com.anoop.gurbanidaily.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.anoop.gurbanidaily.ui.screens.AngBrowseScreen
import com.anoop.gurbanidaily.ui.screens.FavoritesScreen
import com.anoop.gurbanidaily.ui.screens.HistoryScreen
import com.anoop.gurbanidaily.ui.screens.MainScaffold
import com.anoop.gurbanidaily.ui.screens.RaagsScreen
import com.anoop.gurbanidaily.ui.screens.SearchScreen
import com.anoop.gurbanidaily.ui.screens.SettingsScreen
import com.anoop.gurbanidaily.ui.screens.ShabadReaderScreen

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    NavHost(
        navController = nav,
        startDestination = Dest.Main.route,
        enterTransition = { slideInHorizontally { it / 4 } + fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { slideOutHorizontally { it / 4 } + fadeOut() }
    ) {
        composable(Dest.Main.route) {
            MainScaffold(
                onOpenFavorites = { nav.navigate(Dest.Favorites.route) },
                onOpenHistory = { nav.navigate(Dest.History.route) },
                onOpenSettings = { nav.navigate(Dest.Settings.route) },
                onOpenSearch = { nav.navigate(Dest.Search.route) },
                onOpenShabad = { id -> nav.navigate(Dest.Reader.build(id)) },
                onOpenRaags = { nav.navigate(Dest.Raags.route) }
            )
        }
        composable(Dest.Favorites.route) {
            FavoritesScreen(
                onBack = { nav.popBackStack() },
                onOpenShabad = { id -> nav.navigate(Dest.Reader.build(id)) }
            )
        }
        composable(Dest.History.route) {
            HistoryScreen(
                onBack = { nav.popBackStack() },
                onOpenShabad = { id -> nav.navigate(Dest.Reader.build(id)) }
            )
        }
        composable(Dest.Search.route) {
            SearchScreen(
                onBack = { nav.popBackStack() },
                onOpenShabad = { id -> nav.navigate(Dest.Reader.build(id)) }
            )
        }
        composable(Dest.Settings.route) { SettingsScreen(onBack = { nav.popBackStack() }) }
        composable(Dest.Raags.route) {
            RaagsScreen(
                onBack = { nav.popBackStack() },
                onOpenRaag = { raag ->
                    nav.navigate(
                        Dest.AngBrowse.build(
                            start = raag.angStart.coerceAtLeast(1),
                            end = raag.angEnd,
                            raag = "Raag ${raag.english}"
                        )
                    )
                }
            )
        }
        composable(
            route = Dest.Reader.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStack ->
            val id = backStack.arguments?.getString("id") ?: return@composable
            ShabadReaderScreen(shabadId = id, onBack = { nav.popBackStack() })
        }
        composable(
            route = Dest.AngBrowse.route,
            arguments = listOf(
                navArgument("start") { type = NavType.IntType; defaultValue = 1 },
                navArgument("end") { type = NavType.IntType; defaultValue = 0 },
                navArgument("raag") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStack ->
            val start = backStack.arguments?.getInt("start") ?: 1
            val end = backStack.arguments?.getInt("end") ?: 0
            val raag = java.net.URLDecoder.decode(
                backStack.arguments?.getString("raag") ?: "",
                "UTF-8"
            )
            AngBrowseScreen(
                startAng = start,
                endAng = end,
                raagName = raag,
                onBack = { nav.popBackStack() },
                onOpenShabad = { id -> nav.navigate(Dest.Reader.build(id)) }
            )
        }
    }
}
