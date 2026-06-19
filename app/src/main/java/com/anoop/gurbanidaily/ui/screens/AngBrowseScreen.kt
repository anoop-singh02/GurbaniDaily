package com.anoop.gurbanidaily.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anoop.gurbanidaily.data.AngShabadEntry
import com.anoop.gurbanidaily.data.Angs
import com.anoop.gurbanidaily.ui.components.GradientBackdrop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AngBrowseScreen(
    startAng: Int,
    endAng: Int,
    raagName: String,
    onBack: () -> Unit,
    onOpenShabad: (String) -> Unit
) {
    var ang by remember { mutableIntStateOf(startAng.coerceAtLeast(1)) }
    var entries by remember { mutableStateOf(emptyList<AngShabadEntry>()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(ang) {
        loading = true
        error = null
        Angs.fetch(ang).fold(
            onSuccess = { entries = it; loading = false },
            onFailure = { error = it.localizedMessage ?: "Couldn't load ang."; loading = false }
        )
    }

    GradientBackdrop(darkTheme = isSystemInDarkTheme()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(raagName.ifBlank { "Browse SGGS Ji" })
                            Text(
                                "Ang $ang",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 18.dp)
            ) {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = { if (ang > 1) ang-- },
                        enabled = ang > 1
                    ) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = null)
                        Spacer(Modifier.padding(4.dp))
                        Text("Prev")
                    }
                    FilledTonalButton(
                        onClick = { ang++ },
                        enabled = endAng <= 0 || ang < endAng
                    ) {
                        Text("Next")
                        Spacer(Modifier.padding(4.dp))
                        Icon(Icons.Filled.ChevronRight, contentDescription = null)
                    }
                }
                Spacer(Modifier.height(14.dp))

                when {
                    loading -> Box(
                        Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                    error != null -> Text(
                        error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    entries.isEmpty() -> Text(
                        "No shabads on this ang.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(entries, key = { it.shabadId }) { e ->
                            EntryRow(e, onClick = { onOpenShabad("online:${e.shabadId}") })
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryRow(entry: AngShabadEntry, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                entry.firstGurmukhi,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.titleMedium
            )
            if (entry.englishMeaning.isNotBlank()) {
                Text(
                    entry.englishMeaning,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
            }
            if (entry.writer.isNotBlank()) {
                Text(
                    entry.writer,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
