package com.anoop.gurbanidaily.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anoop.gurbanidaily.GurbaniApp
import com.anoop.gurbanidaily.data.Hukamnama
import com.anoop.gurbanidaily.data.HukamnamaRepo
import com.anoop.gurbanidaily.data.Listen
import com.anoop.gurbanidaily.data.ShabadPicker
import com.anoop.gurbanidaily.ui.components.GradientBackdrop
import com.anoop.gurbanidaily.ui.components.PillChip
import com.anoop.gurbanidaily.ui.components.ShabadCard
import com.anoop.gurbanidaily.ui.components.ThreeDotDivider
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShabadReaderScreen(shabadId: String, onBack: () -> Unit) {
    GradientBackdrop(darkTheme = isSystemInDarkTheme()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Shabad") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            if (shabadId.startsWith("online:")) {
                val onlineId = shabadId.removePrefix("online:").toLongOrNull()
                if (onlineId != null) {
                    OnlineShabadBody(padding, onlineId)
                }
            } else {
                LocalShabadBody(padding, shabadId)
            }
        }
    }
}

@Composable
private fun LocalShabadBody(contentPadding: androidx.compose.foundation.layout.PaddingValues, shabadId: String) {
    val context = LocalContext.current
    val app = context.applicationContext as GurbaniApp
    val prefs = app.prefs
    val scope = rememberCoroutineScope()
    val favorites by prefs.favorites.collectAsState(initial = emptySet())
    val fontScale by prefs.fontScale.collectAsState(initial = 1.0f)
    val shabad = ShabadPicker.byId(shabadId) ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(8.dp))
        ShabadCard(
            shabad = shabad,
            fontScale = fontScale,
            isFavorite = shabad.id in favorites,
            onToggleFavorite = { scope.launch { prefs.toggleFavorite(shabad.id) } },
            onShare = {
                val text = "${shabad.gurmukhi}\n\n${shabad.transliteration}\n\n" +
                    "${shabad.meaning}\n\n— ${shabad.source}"
                context.startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        },
                        "Share Shabad"
                    )
                )
            },
            onListen = { Listen.openYouTube(context, shabad) }
        )
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun OnlineShabadBody(
    contentPadding: androidx.compose.foundation.layout.PaddingValues,
    shabadId: Long
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var shabad by remember { mutableStateOf<Hukamnama?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(shabadId) {
        loading = true
        error = null
        scope.launch {
            HukamnamaRepo.fetchShabadById(shabadId).fold(
                onSuccess = { shabad = it; loading = false },
                onFailure = { error = it.localizedMessage ?: "Couldn't load shabad."; loading = false }
            )
        }
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
        when {
            loading -> Box(
                Modifier.fillMaxWidth().padding(48.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            error != null -> Text(
                error!!,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            shabad != null -> OnlineShabadCard(shabad!!, context)
        }
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun OnlineShabadCard(hukamnama: Hukamnama, context: android.content.Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (hukamnama.raag.isNotBlank()) PillChip("Raag ${hukamnama.raag}")
                if (hukamnama.ang.isNotBlank()) PillChip(hukamnama.ang)
            }
            if (hukamnama.writer.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    hukamnama.writer,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                hukamnama.combinedGurmukhi,
                fontSize = 22.sp,
                lineHeight = 36.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            if (hukamnama.combinedTransliteration.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    hukamnama.combinedTransliteration,
                    fontSize = 13.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (hukamnama.combinedMeaning.isNotBlank()) {
                Spacer(Modifier.height(20.dp))
                ThreeDotDivider()
                Spacer(Modifier.height(20.dp))
                Text(
                    hukamnama.combinedMeaning,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilledTonalButton(
                    onClick = {
                        val q = hukamnama.verses.firstOrNull()?.gurmukhi.orEmpty() +
                            " kirtan ${hukamnama.writer}"
                        val url = "https://www.youtube.com/results?search_query=" +
                            URLEncoder.encode(q, StandardCharsets.UTF_8.toString())
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    },
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Listen on YouTube")
                }
                IconButton(onClick = {
                    val text = "${hukamnama.combinedGurmukhi}\n\n${hukamnama.combinedMeaning}\n\n— ${hukamnama.writer}"
                    context.startActivity(
                        Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            },
                            "Share Shabad"
                        )
                    )
                }) {
                    Icon(Icons.Outlined.Share, contentDescription = "Share")
                }
            }
        }
    }
}
