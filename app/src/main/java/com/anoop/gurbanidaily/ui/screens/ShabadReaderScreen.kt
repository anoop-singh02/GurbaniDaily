package com.anoop.gurbanidaily.ui.screens

import android.content.Intent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anoop.gurbanidaily.GurbaniApp
import com.anoop.gurbanidaily.data.OnlineShabad
import com.anoop.gurbanidaily.data.PreviewCache
import com.anoop.gurbanidaily.data.SavedShabadPreview
import com.anoop.gurbanidaily.data.ShabadApi
import com.anoop.gurbanidaily.ui.components.GradientBackdrop
import com.anoop.gurbanidaily.ui.components.OnlineShabadCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShabadReaderScreen(shabadId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as GurbaniApp
    val prefs = app.prefs
    val scope = rememberCoroutineScope()
    val favorites by prefs.favorites.collectAsState(initial = emptySet())
    val fontScale by prefs.fontScale.collectAsState(initial = 1.0f)
    val journal by prefs.journal.collectAsState(initial = emptyMap())

    val id = shabadId.removePrefix("online:").toLongOrNull()
    var shabad by remember { mutableStateOf<OnlineShabad?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(id) {
        if (id == null) {
            error = "Invalid shabad ID"
            loading = false
            return@LaunchedEffect
        }
        loading = true
        error = null
        ShabadApi.fetchById(id).fold(
            onSuccess = {
                shabad = it
                prefs.pushHistory(it.shabadId)
                PreviewCache.save(context, SavedShabadPreview.from(it))
                loading = false
            },
            onFailure = { error = it.localizedMessage ?: "Couldn't load."; loading = false }
        )
    }

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
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding -> Body(padding, shabad, loading, error, favorites, fontScale,
            journalText = shabad?.let { journal[it.shabadId] }.orEmpty(),
            onToggleFav = { s ->
                scope.launch { prefs.toggleFavorite(s.shabadId) }
            },
            onShare = { s ->
                val text = "${s.allGurmukhi}\n\n${s.allEnglish}\n\n— ${s.sourceLabel}"
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
            onJournal = { s, text -> scope.launch { prefs.setJournalEntry(s.shabadId, text) } }
        )}
    }
}

@Composable
private fun Body(
    contentPadding: PaddingValues,
    shabad: OnlineShabad?,
    loading: Boolean,
    error: String?,
    favorites: Set<Long>,
    fontScale: Float,
    journalText: String,
    onToggleFav: (OnlineShabad) -> Unit,
    onShare: (OnlineShabad) -> Unit,
    onJournal: (OnlineShabad, String) -> Unit
) {
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
        when {
            loading -> Box(
                Modifier.fillMaxWidth().padding(48.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            error != null -> Text(
                error,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            shabad != null -> {
                OnlineShabadCard(
                    shabad = shabad,
                    fontScale = fontScale,
                    isFavorite = shabad.shabadId in favorites,
                    onToggleFavorite = { onToggleFav(shabad) },
                    onShare = { onShare(shabad) }
                )
                Spacer(Modifier.height(20.dp))
                JournalEditor(
                    initialText = journalText,
                    onSave = { onJournal(shabad, it) }
                )
            }
        }
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun JournalEditor(initialText: String, onSave: (String) -> Unit) {
    var text by remember(initialText) { mutableStateOf(initialText) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Your reflection (private)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    onSave(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("What did this shabad mean to you today?") },
                minLines = 3,
                shape = RoundedCornerShape(14.dp)
            )
        }
    }
}
