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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.anoop.gurbanidaily.data.GurbaniData
import com.anoop.gurbanidaily.data.Listen
import com.anoop.gurbanidaily.data.OnlineSearch
import com.anoop.gurbanidaily.data.OnlineSearchResult
import com.anoop.gurbanidaily.data.ShabadPicker
import com.anoop.gurbanidaily.ui.components.GradientBackdrop
import com.anoop.gurbanidaily.ui.components.ShabadListRow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onBack: () -> Unit, onOpenShabad: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var online by remember { mutableStateOf(false) }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var onlineResults by remember { mutableStateOf(emptyList<OnlineSearchResult>()) }

    val localResults = remember(query) {
        if (query.isBlank()) GurbaniData.shabads else ShabadPicker.search(query)
    }

    LaunchedEffect(query, online) {
        if (!online || query.isBlank()) return@LaunchedEffect
        delay(450)
        loading = true
        error = null
        OnlineSearch.searchEnglish(query).fold(
            onSuccess = { onlineResults = it },
            onFailure = { error = it.localizedMessage ?: "Couldn't reach BaniDB." }
        )
        loading = false
    }

    GradientBackdrop(darkTheme = isSystemInDarkTheme()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Search") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(if (online) "Search English meaning…" else "Gurmukhi, meaning, source, tag…")
                    },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !online,
                        onClick = { online = false },
                        label = { Text("Bundled (${GurbaniData.shabads.size})") }
                    )
                    FilterChip(
                        selected = online,
                        onClick = { online = true },
                        label = { Text("Online · Full SGGS Ji") }
                    )
                }

                if (online) {
                    when {
                        query.isBlank() -> Text(
                            "Type to search across Sri Guru Granth Sahib Ji via BaniDB.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        loading -> Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                        error != null -> Column {
                            Text(
                                error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(8.dp))
                            FilledTonalButton(onClick = {
                                scope.launch {
                                    loading = true
                                    error = null
                                    OnlineSearch.searchEnglish(query).fold(
                                        onSuccess = { onlineResults = it },
                                        onFailure = { error = it.localizedMessage ?: "Couldn't reach BaniDB." }
                                    )
                                    loading = false
                                }
                            }) { Text("Retry") }
                        }
                        onlineResults.isEmpty() -> Text(
                            "No matches in SGGS Ji.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(onlineResults, key = { it.shabadId }) { r ->
                                OnlineResultRow(
                                    result = r,
                                    onClick = { onOpenShabad("online:${r.shabadId}") }
                                )
                            }
                        }
                    }
                } else {
                    if (localResults.isEmpty()) {
                        Text("No matches.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(localResults, key = { it.id }) { s ->
                                ShabadListRow(
                                    shabad = s,
                                    onListen = { Listen.openYouTube(context, s) },
                                    onClick = { onOpenShabad(s.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnlineResultRow(result: OnlineSearchResult, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                result.gurmukhi,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.titleMedium
            )
            if (result.englishMeaning.isNotBlank()) {
                Text(result.englishMeaning, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
            }
            if (result.source.isNotBlank()) {
                Text(
                    result.source,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
