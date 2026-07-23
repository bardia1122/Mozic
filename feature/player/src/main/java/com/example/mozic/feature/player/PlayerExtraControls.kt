package com.example.mozic.feature.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.mozic.core.designsystem.R as DesignSystemR
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private val SLEEP_TIMER_OPTIONS_MINUTES = listOf(15, 30, 45, 60)
private val PLAYBACK_SPEEDS = listOf(1f, 1.5f, 2f)

/**
 * Bundles the overflow-menu callbacks so [PlayerOverflowMenu] stays under
 * detekt's parameter-count limit.
 */
data class PlayerOverflowMenuActions(
    val onAddToPlaylistClick: () -> Unit,
    val onSleepTimerClick: () -> Unit,
    val onSpeedClick: () -> Unit,
    val onShareClick: () -> Unit,
)

/**
 * Three-dot menu (design handoff: `doc/design_handoff_music_player_controls.md`)
 * replacing the old inline Share/Sleep/Speed icons — each now a labeled row.
 * Tapping a row dismisses the menu and fires its callback; the callback
 * itself opens whatever comes next (a dialog, the share sheet, or navigates
 * to the add-to-playlist sheet) rather than this composable owning that state.
 */
@Composable
fun PlayerOverflowMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    actions: PlayerOverflowMenuActions,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        IconButton(onClick = { onExpandedChange(true) }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(DesignSystemR.string.cd_overflow_menu),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            DropdownMenuItem(
                text = { Text(stringResource(DesignSystemR.string.player_menu_add_to_playlist)) },
                leadingIcon = { Icon(imageVector = Icons.Filled.PlaylistAdd, contentDescription = null) },
                onClick = { onExpandedChange(false); actions.onAddToPlaylistClick() },
            )
            DropdownMenuItem(
                text = { Text(stringResource(DesignSystemR.string.player_menu_sleep_timer)) },
                leadingIcon = { Icon(imageVector = Icons.Filled.Bedtime, contentDescription = null) },
                onClick = { onExpandedChange(false); actions.onSleepTimerClick() },
            )
            DropdownMenuItem(
                text = { Text(stringResource(DesignSystemR.string.player_menu_speed)) },
                leadingIcon = { Icon(imageVector = Icons.Filled.Speed, contentDescription = null) },
                onClick = { onExpandedChange(false); actions.onSpeedClick() },
            )
            DropdownMenuItem(
                text = { Text(stringResource(DesignSystemR.string.player_menu_share)) },
                leadingIcon = { Icon(imageVector = Icons.Filled.Share, contentDescription = null) },
                onClick = { onExpandedChange(false); actions.onShareClick() },
            )
        }
    }
}

/** Duration picker, opened from [PlayerOverflowMenu]'s "Sleep timer" row instead of its own dropdown. */
@Composable
fun SleepTimerDialog(onSetTimer: (Duration?) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(DesignSystemR.string.player_menu_sleep_timer)) },
        text = {
            Column {
                PickerRow(
                    text = stringResource(DesignSystemR.string.player_sleep_timer_off),
                    onClick = { onSetTimer(null); onDismiss() },
                )
                SLEEP_TIMER_OPTIONS_MINUTES.forEach { minutes ->
                    PickerRow(
                        text = stringResource(DesignSystemR.string.player_sleep_timer_minutes, minutes),
                        onClick = { onSetTimer(minutes.minutes); onDismiss() },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(DesignSystemR.string.action_cancel)) }
        },
    )
}

/** Speed picker, opened from [PlayerOverflowMenu]'s "Speed" row instead of its own dropdown. */
@Composable
fun PlaybackSpeedDialog(onSetSpeed: (Float) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(DesignSystemR.string.player_menu_speed)) },
        text = {
            Column {
                PLAYBACK_SPEEDS.forEach { option ->
                    PickerRow(text = formatSpeedLabel(option), onClick = { onSetSpeed(option); onDismiss() })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(DesignSystemR.string.action_cancel)) }
        },
    )
}

@Composable
private fun PickerRow(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Text(text = text, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
    }
}

private fun formatSpeedLabel(speed: Float): String {
    val trimmed = if (speed == speed.toInt().toFloat()) speed.toInt().toString() else speed.toString()
    return "${trimmed}x"
}
