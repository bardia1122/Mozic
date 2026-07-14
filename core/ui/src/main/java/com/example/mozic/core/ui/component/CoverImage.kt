package com.example.mozic.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

/**
 * The one place feature modules load a remote image from. Coil is a
 * `:core:ui`-only dependency — features never import it directly, so the
 * real network image loader can be swapped without touching feature code.
 */
@Composable
fun CoverImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
    )
}
