package com.anoop.gurbanidaily.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.anoop.gurbanidaily.BuildConfig
import com.anoop.gurbanidaily.GurbaniApp
import com.anoop.gurbanidaily.navigation.TopTab
import com.anoop.gurbanidaily.ui.components.FloatingBottomNav
import com.anoop.gurbanidaily.ui.components.GradientBackdrop
import com.anoop.gurbanidaily.ui.components.WhatsNewDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    onOpenFavorites: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenShabad: (String) -> Unit,
    onOpenRaags: () -> Unit,
    onOpenPunjabiMonths: () -> Unit
) {
    var currentTab by rememberSaveable { mutableStateOf(TopTab.Quote.route) }
    val tab = TopTab.fromRoute(currentTab) ?: TopTab.Quote
    val dark = isSystemInDarkTheme()

    val context = LocalContext.current
    val app = context.applicationContext as GurbaniApp
    val prefs = app.prefs
    val scope = rememberCoroutineScope()
    val lastSeen by prefs.lastSeenChangelogBuild.collectAsState(initial = -1)
    var showWhatsNew by remember { mutableStateOf(false) }
    var dismissed by remember { mutableStateOf(false) }

    LaunchedEffect(lastSeen) {
        if (lastSeen < 0 || dismissed) return@LaunchedEffect
        when {
            lastSeen == 0 -> prefs.setLastSeenChangelogBuild(BuildConfig.VERSION_CODE)  // fresh install
            lastSeen < BuildConfig.VERSION_CODE -> showWhatsNew = true                   // update
        }
    }

    if (showWhatsNew) {
        WhatsNewDialog(onDismiss = {
            showWhatsNew = false
            dismissed = true
            scope.launch { prefs.setLastSeenChangelogBuild(BuildConfig.VERSION_CODE) }
        })
    }

    GradientBackdrop(darkTheme = dark) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Daily Gurbani",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    actions = {
                        IconButton(onClick = onOpenFavorites) {
                            Icon(Icons.Outlined.Favorite, contentDescription = "Favourites")
                        }
                        IconButton(onClick = onOpenHistory) {
                            Icon(Icons.Filled.History, contentDescription = "History")
                        }
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
            },
            bottomBar = {
                FloatingBottomNav(
                    selected = tab,
                    onSelect = { currentTab = it.route }
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = tab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab-content"
                ) { t ->
                    when (t) {
                        TopTab.Quote -> QuoteScreen(contentPadding = padding)
                        TopTab.Hukamnama -> HukamnamaScreen(contentPadding = padding)
                        TopTab.Library -> LibraryScreen(
                            contentPadding = padding,
                            onOpenSearch = onOpenSearch,
                            onOpenRaags = onOpenRaags,
                            onOpenHukamnama = { currentTab = TopTab.Hukamnama.route },
                            onOpenPunjabiMonths = onOpenPunjabiMonths
                        )
                    }
                }
            }
        }
    }
}
