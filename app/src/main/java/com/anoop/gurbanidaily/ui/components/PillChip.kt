package com.anoop.gurbanidaily.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PillChip(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = accent,
        modifier = modifier
            .border(width = 1.dp, color = accent.copy(alpha = 0.35f), shape = CircleShape)
            .background(color = accent.copy(alpha = 0.06f), shape = CircleShape)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    )
}
