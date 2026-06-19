package com.anoop.gurbanidaily.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anoop.gurbanidaily.GurbaniApp
import com.anoop.gurbanidaily.data.GurbaniData
import com.anoop.gurbanidaily.data.ShabadPicker
import com.anoop.gurbanidaily.ui.components.DisplayHeader
import com.anoop.gurbanidaily.ui.components.ShabadCard
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun QuoteScreen(contentPadding: PaddingValues) {
    val context = LocalContext.current
    val app = context.applicationContext as GurbaniApp
    val prefs = app.prefs
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val shabads = remember { GurbaniData.shabads }
    val todayIndex = remember {
        shabads.indexOfFirst { it.id == ShabadPicker.shabadForToday().id }.coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(initialPage = todayIndex) { shabads.size }
    val favorites by prefs.favorites.collectAsState(initial = emptySet())
    val fontScale by prefs.fontScale.collectAsState(initial = 1.0f)
    val streak by prefs.streak.collectAsState(initial = 0)
    val dateLabel = remember {
        SimpleDateFormat("EEEE, d MMMM", Locale.ENGLISH).format(Date())
    }

    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        prefs.touchStreak()
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                prefs.pushHistory(shabads[page].id)
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
    }

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
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            DisplayHeader(title = "Today's Quote", subtitle = dateLabel)
            if (streak >= 2) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "🔥 $streak day streak",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(20.dp))

            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 4.dp),
                pageSpacing = 16.dp
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
                    onListen = null,
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

            Spacer(Modifier.height(24.dp))

            FilledTonalButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        val next = (0 until shabads.size).random()
                        pagerState.animateScrollToPage(next)
                    }
                },
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text("Shuffle for another")
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "Swipe ← → for more · Pull down to shuffle",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(120.dp))
        }
    }
}
