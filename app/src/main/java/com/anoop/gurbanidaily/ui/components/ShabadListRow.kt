package com.anoop.gurbanidaily.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anoop.gurbanidaily.data.Shabad

@Composable
fun ShabadListRow(
    shabad: Shabad,
    onListen: () -> Unit,
    onClick: () -> Unit = onListen
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    shabad.gurmukhi.lineSequence().first(),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Text(shabad.meaning, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
                Text(
                    shabad.source,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            IconButton(onClick = onListen) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Listen on YouTube",
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
