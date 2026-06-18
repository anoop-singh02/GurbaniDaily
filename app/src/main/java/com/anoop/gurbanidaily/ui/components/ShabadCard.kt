package com.anoop.gurbanidaily.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anoop.gurbanidaily.data.Shabad

@Composable
fun ShabadCard(
    shabad: Shabad,
    fontScale: Float,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    onListen: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        AnimatedContent(
            targetState = shabad,
            transitionSpec = {
                (slideInVertically { it / 6 } + fadeIn()) togetherWith
                    (slideOutVertically { -it / 6 } + fadeOut())
            },
            label = "shabad-transition"
        ) { s ->
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = s.gurmukhi,
                    fontSize = (24 * fontScale).sp,
                    lineHeight = (38 * fontScale).sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(18.dp))
                Text(
                    text = s.transliteration,
                    fontSize = (15 * fontScale).sp,
                    lineHeight = (24 * fontScale).sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(18.dp))
                HorizontalDivider(Modifier.fillMaxWidth(0.4f))
                Spacer(Modifier.height(18.dp))
                Text(
                    text = s.meaning,
                    fontSize = (16 * fontScale).sp,
                    lineHeight = (26 * fontScale).sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    text = s.source,
                    fontSize = (13 * fontScale).sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.height(16.dp))
                FilledTonalButton(onClick = onListen) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Listen on YouTube")
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite
                            else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favourite",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
