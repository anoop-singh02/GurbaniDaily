package com.anoop.gurbanidaily.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Refresh
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
import com.anoop.gurbanidaily.data.HukamnamaVerse
import com.anoop.gurbanidaily.ui.components.DisplayHeader
import com.anoop.gurbanidaily.ui.components.PillChip
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HukamnamaScreen(contentPadding: PaddingValues) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hukamnama by remember { mutableStateOf<Hukamnama?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }

    fun loadToday(force: Boolean) {
        loading = true
        error = null
        selectedDate = null
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

    fun loadDate(cal: Calendar) {
        loading = true
        error = null
        selectedDate = cal
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(cal.time)
        scope.launch {
            HukamnamaRepo.fetchForDate(date).fold(
                onSuccess = { hukamnama = it; error = null },
                onFailure = { error = it.localizedMessage ?: "Couldn't load for $date." }
            )
            loading = false
        }
    }

    LaunchedEffect(Unit) { loadToday(force = false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        DisplayHeader(
            title = "Hukamnama Sahib",
            subtitle = "Sri Harmandir Sahib · Amritsar"
        )
        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilledTonalButton(
                onClick = {
                    val now = Calendar.getInstance()
                    val start = selectedDate ?: now
                    DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            val c = Calendar.getInstance().apply { set(y, m, d) }
                            loadDate(c)
                        },
                        start.get(Calendar.YEAR),
                        start.get(Calendar.MONTH),
                        start.get(Calendar.DAY_OF_MONTH)
                    ).apply {
                        datePicker.maxDate = now.timeInMillis
                    }.show()
                },
                shape = CircleShape
            ) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null,
                    modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (selectedDate != null) "Change date" else "Past Hukamnamas")
            }
            if (selectedDate != null) {
                FilledTonalButton(
                    onClick = { loadToday(force = true) },
                    shape = CircleShape
                ) { Text("Today") }
            }
        }

        Spacer(Modifier.height(20.dp))

        when {
            loading && hukamnama == null -> Box(
                modifier = Modifier.fillMaxWidth().padding(48.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            hukamnama != null -> HukamnamaCard(
                hukamnama = hukamnama!!,
                onReload = {
                    if (selectedDate != null) loadDate(selectedDate!!) else loadToday(force = true)
                },
                isRefreshing = loading
            )
            error != null -> ErrorCard(message = error!!, onReload = { loadToday(force = true) })
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
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
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

            if (hukamnama.ang.isNotEmpty() || hukamnama.raag.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (hukamnama.raag.isNotEmpty()) PillChip("Raag ${hukamnama.raag}")
                    if (hukamnama.ang.isNotEmpty()) PillChip(hukamnama.ang)
                }
            }
            if (hukamnama.writer.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    hukamnama.writer,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(Modifier.height(22.dp))

            hukamnama.verses.forEachIndexed { i, verse ->
                VerseBlock(verse)
                if (i < hukamnama.verses.lastIndex) {
                    Spacer(Modifier.height(18.dp))
                    HorizontalDivider(
                        Modifier.fillMaxWidth(0.35f),
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(18.dp))
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconButton(onClick = {
                    val text = "Hukamnama Sahib — ${hukamnama.dateLabel}\n\n" +
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
private fun VerseBlock(verse: HukamnamaVerse) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            verse.gurmukhi,
            fontSize = 24.sp,
            lineHeight = 40.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
        if (verse.punjabiMeaning.isNotBlank()) {
            Spacer(Modifier.height(14.dp))
            Text(
                verse.punjabiMeaning,
                fontSize = 16.sp,
                lineHeight = 26.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (verse.englishMeaning.isNotBlank()) {
            Spacer(Modifier.height(14.dp))
            Text(
                verse.englishMeaning,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
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
            Text("Couldn't load this Hukamnama", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            FilledTonalButton(onClick = onReload, shape = CircleShape) {
                Icon(Icons.Outlined.Refresh, contentDescription = null,
                    modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Try again")
            }
        }
    }
}
