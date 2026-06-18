package com.anoop.gurbanidaily.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anoop.gurbanidaily.GurbaniApp
import com.anoop.gurbanidaily.data.GurbaniData
import com.anoop.gurbanidaily.data.ShabadPicker
import com.anoop.gurbanidaily.ui.components.ShabadCard
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenFavorites: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as GurbaniApp
    val prefs = app.prefs
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val shabads = remember { GurbaniData.shabads }
    val todayIndex = remember { shabads.indexOfFirst { it.id == ShabadPicker.shabadForToday().id }.coerceAtLeast(0) }
    val pagerState = rememberPagerState(initialPage = todayIndex) { shabads.size }
    val favorites by prefs.favorites.collectAsState(initial = emptySet())
    val fontScale by prefs.fontScale.collectAsState(initial = 1.0f)

    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                prefs.pushHistory(shabads[page].id)
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Daily Gurbani", fontWeight = FontWeight.Medium) },
                actions = {
                    IconButton(onClick = onOpenSearch) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = onOpenFavorites) {
                        Icon(Icons.Outlined.Favorite, contentDescription = "Favourites")
                    }
                    IconButton(onClick = onOpenHistory) {
                        Icon(Icons.Filled.History, contentDescription = "History")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    val next = (0 until shabads.size).random()
                    pagerState.animateScrollToPage(next)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isRefreshing = false
                }
            },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    pageSpacing = 12.dp
                ) { page ->
                    val s = shabads[page]
                    ShabadCard(
                        shabad = s,
                        fontScale = fontScale,
                        isFavorite = s.id in favorites,
                        onToggleFavorite = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch { prefs.toggleFavorite(s.id) }
                        },
                        onListen = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            com.anoop.gurbanidaily.data.Listen.openYouTube(context, s)
                        },
                        onShare = {
                            val text = "${s.gurmukhi}\n\n${s.transliteration}\n\n${s.meaning}\n\n— ${s.source}"
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(send, "Share Shabad"))
                        }
                    )
                }
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilledTonalButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch {
                            val next = (0 until shabads.size).random()
                            pagerState.animateScrollToPage(next)
                        }
                    }) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Shuffle")
                    }
                    TextButton(onClick = {
                        scope.launch { pagerState.animateScrollToPage(todayIndex) }
                    }) {
                        Text("Today")
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Swipe ←/→ for next or previous • Pull down to shuffle",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
