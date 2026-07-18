package com.example.mozic.feature.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private const val VISUALIZER_BAR_COUNT = 32
private const val VISUALIZER_CYCLES_PER_SECOND = 1.4f
private const val VISUALIZER_MIN_HEIGHT_FRACTION = 0.08f
private const val VISUALIZER_RANDOM_SEED = 20260718L
private val TWO_PI = (2 * PI).toFloat()

/**
 * Simulated bar-set visualizer for the Now Playing screen (A6, option 2 from
 * `doc/CLAUDE_PERSON_A.md` §5.6) — no `RECORD_AUDIO` permission, no real FFT
 * data, just per-bar phase- and frequency-shifted sines so the bars don't move
 * in lockstep. Drawn with Compose Canvas per the spec (Lottie/GIF forbidden).
 *
 * Time is accumulated in a `remember`ed float only while [isPlaying], the same
 * pattern `RotatingCover` uses for its spin — so the bars freeze in place
 * instead of resetting to a flat line on pause.
 */
@Composable
fun AudioVisualizer(isPlaying: Boolean, modifier: Modifier = Modifier, barCount: Int = VISUALIZER_BAR_COUNT) {
    var elapsedSeconds by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        var lastFrameNanos = withFrameNanos { it }
        while (true) {
            withFrameNanos { nowNanos ->
                elapsedSeconds += (nowNanos - lastFrameNanos) / 1_000_000_000f
                lastFrameNanos = nowNanos
            }
        }
    }

    // Fixed seed: each bar's phase/frequency offset must stay stable across
    // recompositions, only the seed value itself is arbitrary.
    val barSeeds = remember(barCount) {
        val random = Random(VISUALIZER_RANDOM_SEED)
        List(barCount) { (random.nextFloat() * TWO_PI) to (0.7f + random.nextFloat() * 0.6f) }
    }
    val barColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier.fillMaxWidth()) {
        val barWidth = size.width / (barCount * 2 - 1)
        barSeeds.forEachIndexed { index, (phase, frequencyScale) ->
            val amplitude = if (isPlaying) {
                val wave = sin(elapsedSeconds * VISUALIZER_CYCLES_PER_SECOND * frequencyScale * TWO_PI + phase)
                ((wave + 1f) / 2f).coerceIn(VISUALIZER_MIN_HEIGHT_FRACTION, 1f)
            } else {
                VISUALIZER_MIN_HEIGHT_FRACTION
            }
            val barHeight = size.height * amplitude
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x = index * barWidth * 2, y = size.height - barHeight),
                size = Size(width = barWidth, height = barHeight),
                cornerRadius = CornerRadius(barWidth / 2),
            )
        }
    }
}
