package com.example.mozic.core.ui.modifier

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

internal const val ARTWORK_STRIPE_ALPHA = 0.04f
private const val STRIPE_SPACING_DP = 10f
private const val STRIPE_WIDTH_PX = 2f

/**
 * `surfaceElevated` fill with a faint diagonal repeating-stripe texture,
 * standing in for real cover art (DESIGN.md §4). Used for every cover-less
 * or not-yet-loaded artwork spot instead of a flat empty rectangle, so the
 * pattern reads as "placeholder, image goes here" rather than a bug.
 */
fun Modifier.artworkPlaceholder(): Modifier = composed {
    val base = MaterialTheme.colorScheme.surfaceVariant
    val stripe = Color.White.copy(alpha = ARTWORK_STRIPE_ALPHA)
    background(base).drawWithContent {
        drawContent()
        drawArtworkStripes(stripe)
    }
}

/** Shared with [com.example.mozic.core.ui.component.rememberArtworkPlaceholderPainter]. */
internal fun DrawScope.drawArtworkStripes(stripeColor: Color) {
    val spacingPx = STRIPE_SPACING_DP.dp.toPx()
    val diagonal = size.width + size.height
    var offset = -size.height
    while (offset < diagonal) {
        drawLine(
            color = stripeColor,
            start = Offset(offset, 0f),
            end = Offset(offset + size.height, size.height),
            strokeWidth = STRIPE_WIDTH_PX,
        )
        offset += spacingPx
    }
}
