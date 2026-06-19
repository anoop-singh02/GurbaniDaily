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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anoop.gurbanidaily.data.Categories
import com.anoop.gurbanidaily.data.Category
import com.anoop.gurbanidaily.ui.components.DisplayHeader

@Composable
fun LibraryScreen(
    contentPadding: PaddingValues,
    onOpenCategory: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenShabad: (String) -> Unit,
    onOpenRaags: () -> Unit
) {
    val cats = Categories.all
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding(),
            start = 18.dp,
            end = 18.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            Column {
                Spacer(Modifier.height(8.dp))
                DisplayHeader(title = "Explore Shabads", subtitle = "Browse by tradition")
                Spacer(Modifier.height(20.dp))
                SearchPrompt(onOpenSearch)
                Spacer(Modifier.height(14.dp))
                NitnemQuickLaunch(onOpenCategory)
                Spacer(Modifier.height(14.dp))
                RaagsTile(onOpenRaags)
                Spacer(Modifier.height(14.dp))
                Text(
                    "Categories",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(cats, key = { it.id }) { c ->
            CategoryTile(category = c, onClick = { onOpenCategory(c.id) })
        }
        item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(120.dp)) }
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
            Text("🔍", fontSize = 18.sp)
            Spacer(Modifier.padding(6.dp))
            Text(
                "Search gurmukhi, meaning, or Full SGGS Ji…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NitnemQuickLaunch(onOpenCategory: (String) -> Unit) {
    Column {
        Text(
            "Daily Nitnem",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NitnemChip("☀️ Japji", onOpenCategory)
            NitnemChip("🌅 Rehras", onOpenCategory)
            NitnemChip("🌙 Sohila", onOpenCategory)
        }
    }
}

@Composable
private fun NitnemChip(label: String, onOpenCategory: (String) -> Unit) {
    AssistChip(
        onClick = { onOpenCategory("nitnem") },
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        shape = CircleShape
    )
}

@Composable
private fun RaagsTile(onOpenRaags: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenRaags),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.AutoStories,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.padding(6.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Browse by Raag",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "31 raags of Sri Guru Granth Sahib Ji · online",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CategoryTile(category: Category, onClick: () -> Unit) {
    val count = Categories.shabadsIn(category).size
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(category.emoji, fontSize = 28.sp)
            Column {
                Text(
                    category.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "$count · ${category.subtitle}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
