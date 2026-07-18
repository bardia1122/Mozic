package com.example.mozic.core.ui.color

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Cover art's dominant/vibrant swatch, re-extracted whenever [model] changes.
 * Feeds the Now Playing screen's palette-tinted background (A5) without any
 * feature module importing Coil or Palette directly — same encapsulation
 * rationale as [com.example.mozic.core.ui.component.CoverImage].
 *
 * `allowHardware(false)` is mandatory: Palette reads raw pixels and crashes
 * on a hardware bitmap. The Palette pass itself is CPU work, so it's hopped
 * onto [Dispatchers.Default]; the network/decode fetch through Coil's own
 * [coil3.ImageLoader] is already off the main thread.
 */
@Composable
fun rememberDominantColor(model: Any?, fallback: Color): State<Color> {
    val context = LocalContext.current
    val colorState = remember { mutableStateOf(fallback) }
    LaunchedEffect(model) {
        colorState.value = model?.let { extractDominantColor(context, it) } ?: fallback
    }
    return colorState
}

private suspend fun extractDominantColor(context: Context, model: Any): Color? {
    val request = ImageRequest.Builder(context)
        .data(model)
        .allowHardware(false)
        .build()
    val result = context.imageLoader.execute(request)
    if (result !is SuccessResult) return null
    return withContext(Dispatchers.Default) {
        val palette = Palette.from(result.image.toBitmap()).generate()
        val swatch = palette.vibrantSwatch ?: palette.dominantSwatch
        swatch?.let { Color(it.rgb) }
    }
}
