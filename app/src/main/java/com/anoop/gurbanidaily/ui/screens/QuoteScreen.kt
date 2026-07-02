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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anoop.gurbanidaily.GurbaniApp
import com.anoop.gurbanidaily.data.DailyQuote
import com.anoop.gurbanidaily.data.DailyInsight
import com.anoop.gurbanidaily.data.DailyInsightBuilder
import com.anoop.gurbanidaily.data.NanakshahiCalendar
import com.anoop.gurbanidaily.data.OnlineShabad
import com.anoop.gurbanidaily.data.PreviewCache
import com.anoop.gurbanidaily.data.SavedShabadPreview
import com.anoop.gurbanidaily.ui.components.DisplayHeader
import com.anoop.gurbanidaily.ui.components.PillChip
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QuoteScreen(
    contentPadding: PaddingValues,
    onOpenShabad: (String) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as GurbaniApp
    val prefs = app.prefs
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val favorites by prefs.favorites.collectAsState(initial = emptySet())
    val fontScale by prefs.fontScale.collectAsState(initial = 1.0f)
    val streak by prefs.streak.collectAsState(initial = 0)
    val dateLabel = remember {
        val greg = SimpleDateFormat("EEEE, d MMMM", Locale.ENGLISH).format(Date())
        val nan = NanakshahiCalendar.currentMonth()
        "$greg  ·  ${nan.gurmukhi} ${nan.english}"
    }

    var shabad by remember { mutableStateOf<OnlineShabad?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load(force: Boolean) {
        loading = true
        error = null
        scope.launch {
            val result = if (force) DailyQuote.forceNew(context) else DailyQuote.getForToday(context)
            result.fold(
                onSuccess = {
                    shabad = it
                    prefs.pushHistory(it.shabadId)
                    PreviewCache.save(context, SavedShabadPreview.from(it))
                },
                onFailure = { error = it.localizedMessage ?: "Couldn't fetch a shabad." }
            )
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        prefs.touchStreak()
        load(force = false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        DisplayHeader(title = "Today's Reflection", subtitle = dateLabel)
        if (streak >= 2) {
            Spacer(Modifier.height(10.dp))
            Text(
                "🔥 $streak day streak",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(24.dp))

        when {
            loading && shabad == null -> Box(
                Modifier.fillMaxWidth().padding(64.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            error != null && shabad == null -> Text(
                error!!,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            shabad != null -> {
                val s = shabad!!
                val insight = remember(s.shabadId) { DailyInsightBuilder.from(s) }
                DailyReflectionCard(
                    shabad = s,
                    insight = insight,
                    fontScale = fontScale,
                    isFavorite = s.shabadId in favorites,
                    onToggleFavorite = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch { prefs.toggleFavorite(s.shabadId) }
                    },
                    onOpenFullShabad = { onOpenShabad("online:${s.shabadId}") },
                    onShare = {
                        val text = "${insight.gurmukhiLine}\n\n" +
                            "Meaning:\n${insight.meaning}\n\n" +
                            "For today:\n${insight.reflection}\n\n" +
                            "— ${s.sourceLabel}"
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(Intent.createChooser(send, "Share Shabad"))
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        FilledTonalButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                load(force = true)
            },
            shape = CircleShape,
            enabled = !loading
        ) {
            Icon(Icons.Filled.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text("New quote")
        }
        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun DailyReflectionCard(
    shabad: OnlineShabad,
    insight: DailyInsight,
    fontScale: Float,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onOpenFullShabad: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (shabad.raagEnglish.isNotBlank()) {
                    PillChip(com.anoop.gurbanidaily.data.formatRaag(shabad.raagEnglish))
                }
                if (shabad.ang > 0) PillChip("Ang ${shabad.ang}")
            }
            if (shabad.writerEnglish.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    shabad.writerEnglish,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(Modifier.height(22.dp))
            Text(
                insight.gurmukhiLine,
                fontSize = (26 * fontScale).sp,
                lineHeight = (40 * fontScale).sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(22.dp))
            HorizontalDivider(
                Modifier.fillMaxWidth(0.35f),
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(18.dp))

            Text(
                "Meaning",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                insight.meaning,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(18.dp))
            Text(
                "For today",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                insight.reflection,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))
            FilledTonalButton(onClick = onOpenFullShabad, shape = CircleShape) {
                Text("Read full shabad")
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite
                        else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favourite",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onShare) {
                    Icon(
                        Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
