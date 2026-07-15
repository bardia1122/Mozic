package com.example.mozic.core.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import com.example.mozic.core.ui.modifier.ARTWORK_STRIPE_ALPHA
import com.example.mozic.core.ui.modifier.drawArtworkStripes

private class ArtworkPlaceholderPainter(
    private val baseColor: Color,
    private val stripeColor: Color,
) : Painter() {
    override val intrinsicSize: Size = Size.Unspecified

    override fun DrawScope.onDraw() {
        drawRect(baseColor)
        drawArtworkStripes(stripeColor)
    }
}

/** Coil `placeholder`/`error` [Painter] version of `Modifier.artworkPlaceholder()`, for [CoverImage]. */
@Composable
fun rememberArtworkPlaceholderPainter(): Painter {
    val base = MaterialTheme.colorScheme.surfaceVariant
    val stripe = Color.White.copy(alpha = ARTWORK_STRIPE_ALPHA)
    return remember(base, stripe) { ArtworkPlaceholderPainter(base, stripe) }
}
