package com.anoop.gurbanidaily.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.anoop.gurbanidaily.ui.screens.CategoryScreen
import com.anoop.gurbanidaily.ui.screens.FavoritesScreen
import com.anoop.gurbanidaily.ui.screens.HistoryScreen
import com.anoop.gurbanidaily.ui.screens.MainScaffold
import com.anoop.gurbanidaily.ui.screens.SearchScreen
import com.anoop.gurbanidaily.ui.screens.SettingsScreen

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
                onOpenCategory = { id -> nav.navigate(Dest.Category.build(id)) }
            )
        }
        composable(Dest.Favorites.route) { FavoritesScreen(onBack = { nav.popBackStack() }) }
        composable(Dest.History.route) { HistoryScreen(onBack = { nav.popBackStack() }) }
        composable(Dest.Search.route) { SearchScreen(onBack = { nav.popBackStack() }) }
        composable(Dest.Settings.route) { SettingsScreen(onBack = { nav.popBackStack() }) }
        composable(
            route = Dest.Category.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStack ->
            val id = backStack.arguments?.getString("id") ?: return@composable
            CategoryScreen(categoryId = id, onBack = { nav.popBackStack() })
        }
    }
}
