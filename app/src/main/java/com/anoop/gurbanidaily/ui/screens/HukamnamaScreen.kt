package com.anoop.gurbanidaily.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anoop.gurbanidaily.data.Hukamnama
import com.anoop.gurbanidaily.data.HukamnamaRepo
import com.anoop.gurbanidaily.ui.components.DisplayHeader
import com.anoop.gurbanidaily.ui.components.PillChip
import com.anoop.gurbanidaily.ui.components.ThreeDotDivider
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun HukamnamaScreen(contentPadding: PaddingValues) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hukamnama by remember { mutableStateOf<Hukamnama?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun reload(force: Boolean) {
        loading = true
        error = null
        scope.launch {
            val result = if (force) HukamnamaRepo.fetchToday(context)
            else HukamnamaRepo.loadCachedOrFetch(context)
            result.fold(
                onSuccess = { hukamnama = it; error = null },
                onFailure = { error = it.localizedMessage ?: "Couldn't reach BaniDB." }
            )
            loading = false
        }
    }

    LaunchedEffect(Unit) { reload(force = false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        DisplayHeader(
            title = "Hukamnama",
            subtitle = "Sri Harmandir Sahib · Amritsar"
        )
        Spacer(Modifier.height(24.dp))

        when {
            loading && hukamnama == null -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(48.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            hukamnama != null -> HukamnamaCard(
                hukamnama = hukamnama!!,
                onReload = { reload(force = true) },
                isRefreshing = loading
            )
            error != null -> ErrorCard(message = error!!, onReload = { reload(force = true) })
        }
        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun HukamnamaCard(
    hukamnama: Hukamnama,
    onReload: () -> Unit,
    isRefreshing: Boolean
) {
    val context = LocalContext.current
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    hukamnama.dateLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = onReload) {
                    Icon(
                        Icons.Outlined.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (hukamnama.ang.isNotEmpty() || hukamnama.raag.isNotEmpty() || hukamnama.writer.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (hukamnama.raag.isNotEmpty()) PillChip("Raag ${hukamnama.raag}")
                    if (hukamnama.ang.isNotEmpty()) PillChip(hukamnama.ang)
                }
                if (hukamnama.writer.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        hukamnama.writer,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                hukamnama.combinedGurmukhi,
                fontSize = 24.sp,
                lineHeight = 40.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )

            if (hukamnama.verses.any { it.transliteration.isNotBlank() }) {
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

            Spacer(Modifier.height(22.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilledTonalButton(
                    onClick = {
                        val q = "Hukamnama Sahib Sri Harmandir Sahib today kirtan"
                        val url = "https://www.youtube.com/results?search_query=" +
                            URLEncoder.encode(q, StandardCharsets.UTF_8.toString())
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    },
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Listen on YouTube")
                }
                IconButton(onClick = {
                    val text =
                        "Hukamnama Sahib — ${hukamnama.dateLabel}\n\n" +
                            "${hukamnama.combinedGurmukhi}\n\n${hukamnama.combinedMeaning}"
                    context.startActivity(
                        Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            },
                            "Share Hukamnama"
                        )
                    )
                }) {
                    Icon(Icons.Outlined.Share, contentDescription = "Share")
                }
            }

            if (isRefreshing) {
                Spacer(Modifier.height(12.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String, onReload: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Couldn't load today's Hukamnama", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            FilledTonalButton(onClick = onReload, shape = CircleShape) {
                Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Try again")
            }
        }
    }
}
