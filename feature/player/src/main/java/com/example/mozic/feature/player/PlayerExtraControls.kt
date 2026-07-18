package com.example.mozic.feature.player

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.mozic.core.designsystem.R as DesignSystemR
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private val SLEEP_TIMER_OPTIONS_MINUTES = listOf(15, 30, 45, 60)
private val PLAYBACK_SPEEDS = listOf(1f, 1.5f, 2f)

/**
 * Bedtime icon + dropdown of duration presets (`doc/CLAUDE_PERSON_A.md` §5.6);
 * tinted primary while a timer is running so it reads as "active" from the
 * top bar without needing its own countdown label.
 */
@Composable
fun SleepTimerButton(isActive: Boolean, onSetTimer: (Duration?) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Filled.Bedtime,
                contentDescription = stringResource(DesignSystemR.string.cd_sleep_timer),
                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(DesignSystemR.string.player_sleep_timer_off)) },
                onClick = { onSetTimer(null); expanded = false },
            )
            SLEEP_TIMER_OPTIONS_MINUTES.forEach { minutes ->
                DropdownMenuItem(
                    text = { Text(stringResource(DesignSystemR.string.player_sleep_timer_minutes, minutes)) },
                    onClick = { onSetTimer(minutes.minutes); expanded = false },
                )
            }
        }
    }
}

/** Cycles 1x/1.5x/2x via a small text dropdown — ExoPlayer time-stretches, so pitch stays correct. */
@Composable
fun PlaybackSpeedButton(speed: Float, onSetSpeed: (Float) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        TextButton(onClick = { expanded = true }) {
            Text(text = formatSpeedLabel(speed), style = MaterialTheme.typography.labelLarge)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            PLAYBACK_SPEEDS.forEach { option ->
                DropdownMenuItem(
                    text = { Text(formatSpeedLabel(option)) },
                    onClick = { onSetSpeed(option); expanded = false },
                )
            }
        }
    }
}

private fun formatSpeedLabel(speed: Float): String {
    val trimmed = if (speed == speed.toInt().toFloat()) speed.toInt().toString() else speed.toString()
    return "${trimmed}x"
}
