package com.example.mozic.core.ui.modifier

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.example.mozic.core.designsystem.theme.mozicColors

private const val SHIMMER_TRAVEL_PX = 1000f
private const val SHIMMER_WIDTH_PX = 400f
private const val SHIMMER_DURATION_MS = 1200

/**
 * Theme-derived skeleton shimmer: a soft highlight sweeping across
 * `surfaceElevated`. Every loading placeholder in the app uses this one
 * modifier so skeletons look consistent across screens.
 */
fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = -SHIMMER_WIDTH_PX,
        targetValue = SHIMMER_TRAVEL_PX,
        animationSpec = infiniteRepeatable(
            animation = tween(SHIMMER_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslateX",
    )
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.mozicColors.shimmerHighlight
    background(
        brush = Brush.linearGradient(
            colors = listOf(base, highlight, base),
            start = Offset(translateX - SHIMMER_WIDTH_PX, 0f),
            end = Offset(translateX, 0f),
        ),
    )
}
