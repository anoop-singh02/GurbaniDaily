package com.anoop.gurbanidaily.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anoop.gurbanidaily.data.NanakshahiCalendar
import com.anoop.gurbanidaily.data.ShabadApi
import com.anoop.gurbanidaily.ui.components.DisplayHeader
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    contentPadding: PaddingValues,
    onOpenSearch: () -> Unit,
    onOpenShabad: (String) -> Unit,
    onOpenRaags: () -> Unit,
    onOpenHukamnama: () -> Unit,
    onOpenPunjabiMonths: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var loadingRandom by remember { mutableStateOf(false) }
    val currentMonth = remember { NanakshahiCalendar.currentMonth() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        DisplayHeader(title = "Explore", subtitle = "Sri Guru Granth Sahib Ji · online")
        Spacer(Modifier.height(24.dp))

        SearchPrompt(onOpenSearch)
        Spacer(Modifier.height(12.dp))

        BigTile(
            icon = Icons.Outlined.Shuffle,
            title = "Random Shabad",
            subtitle = "Pick any shabad from the 1430 angs",
            loading = loadingRandom,
            onClick = {
                if (loadingRandom) return@BigTile
                loadingRandom = true
                scope.launch {
                    ShabadApi.fetchRandom().fold(
                        onSuccess = { s -> onOpenShabad("online:${s.shabadId}") },
                        onFailure = { /* no-op; user can retry */ }
                    )
                    loadingRandom = false
                }
            }
        )
        Spacer(Modifier.height(12.dp))
        BigTile(
            icon = Icons.Outlined.WbSunny,
            title = "Today's Hukamnama",
            subtitle = "From Sri Harmandir Sahib, Amritsar",
            onClick = onOpenHukamnama
        )
        Spacer(Modifier.height(12.dp))
        BigTile(
            icon = Icons.Outlined.CalendarMonth,
            title = "Punjabi Months",
            subtitle = "${currentMonth.gurmukhi} ${currentMonth.english} now · Sangrand dates",
            onClick = onOpenPunjabiMonths
        )
        Spacer(Modifier.height(12.dp))
        BigTile(
            icon = Icons.Outlined.AutoStories,
            title = "Browse by Raag",
            subtitle = "31 raags of SGGS Ji",
            onClick = onOpenRaags
        )

        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun SearchPrompt(onOpenSearch: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenSearch),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "Search English meaning across SGGS Ji…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BigTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (loading) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}
