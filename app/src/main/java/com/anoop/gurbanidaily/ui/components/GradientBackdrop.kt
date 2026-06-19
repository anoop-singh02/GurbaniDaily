package com.anoop.gurbanidaily.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.anoop.gurbanidaily.ui.theme.PeachAccent
import com.anoop.gurbanidaily.ui.theme.SaffronGlow

@Composable
fun GradientBackdrop(
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val accent = if (darkTheme) SaffronGlow else PeachAccent
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .alpha(if (darkTheme) 0.7f else 0.55f)
                .background(
                    Brush.verticalGradient(
                        0f to accent,
                        1f to Color.Transparent
                    )
                )
        )
        content()
    }
}
