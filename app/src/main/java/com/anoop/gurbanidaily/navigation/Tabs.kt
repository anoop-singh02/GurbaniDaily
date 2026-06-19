package com.anoop.gurbanidaily.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopTab(val route: String, val label: String, val icon: ImageVector) {
    Quote("tab/quote", "Today", Icons.Outlined.LightMode),
    Hukamnama("tab/hukamnama", "Hukam", Icons.Outlined.WbSunny),
    Library("tab/library", "Library", Icons.Outlined.AutoStories);

    companion object {
        fun fromRoute(route: String?): TopTab? = entries.firstOrNull { it.route == route }
    }
}
