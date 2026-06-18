package com.anoop.gurbanidaily.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anoop.gurbanidaily.ui.screens.FavoritesScreen
import com.anoop.gurbanidaily.ui.screens.HistoryScreen
import com.anoop.gurbanidaily.ui.screens.HomeScreen
import com.anoop.gurbanidaily.ui.screens.SearchScreen
import com.anoop.gurbanidaily.ui.screens.SettingsScreen

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    NavHost(
        navController = nav,
        startDestination = Dest.Home.route,
        enterTransition = { slideInHorizontally { it / 4 } + fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { slideOutHorizontally { it / 4 } + fadeOut() }
    ) {
        composable(Dest.Home.route) {
            HomeScreen(
                onOpenFavorites = { nav.navigate(Dest.Favorites.route) },
                onOpenHistory = { nav.navigate(Dest.History.route) },
                onOpenSearch = { nav.navigate(Dest.Search.route) },
                onOpenSettings = { nav.navigate(Dest.Settings.route) }
            )
        }
        composable(Dest.Favorites.route) { FavoritesScreen(onBack = { nav.popBackStack() }) }
        composable(Dest.History.route) { HistoryScreen(onBack = { nav.popBackStack() }) }
        composable(Dest.Search.route) { SearchScreen(onBack = { nav.popBackStack() }) }
        composable(Dest.Settings.route) { SettingsScreen(onBack = { nav.popBackStack() }) }
    }
}
