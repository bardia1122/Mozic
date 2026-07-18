package com.example.mozic.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mozic.core.designsystem.R as DesignSystemR
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.designsystem.theme.mozicColors
import com.example.mozic.core.domain.model.PlayerState
import com.example.mozic.core.domain.model.Song
import com.example.mozic.core.ui.component.CoverImage
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = stringResource(DesignSystemR.string.cd_collapse_player),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val song = state.currentSong
        val contentModifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()

        if (song == null) {
            NothingPlayingMessage(modifier = contentModifier)
        } else {
            NowPlayingContent(
                song = song,
                state = state,
                onPlayPauseClick = viewModel::togglePlayPause,
                onNext = viewModel::next,
                onPrevious = viewModel::previous,
                onSeekFinished = viewModel::seekTo,
                modifier = contentModifier,
            )
        }
    }
}

@Composable
private fun NothingPlayingMessage(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(DesignSystemR.string.state_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NowPlayingContent(
    song: Song,
    state: PlayerState,
    onPlayPauseClick: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekFinished: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = MaterialTheme.dimens.screenHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CoverImage(
            model = song.coverImageUrl,
            contentDescription = song.title,
            modifier = Modifier
                .size(MaterialTheme.dimens.nowPlayingCoverSize)
                .clip(CircleShape),
        )
        Spacer(Modifier.height(MaterialTheme.dimens.spaceXl))
        Text(
            text = song.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(MaterialTheme.dimens.spaceXxs))
        Text(
            text = song.artistName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(MaterialTheme.dimens.spaceLg))

        PlayerSeekBar(
            positionMs = state.positionMs,
            durationMs = state.durationMs,
            onSeekFinished = onSeekFinished,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(MaterialTheme.dimens.spaceLg))

        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceXl),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPrevious) {
                Icon(
                    imageVector = Icons.Filled.SkipPrevious,
                    contentDescription = stringResource(DesignSystemR.string.cd_previous),
                    modifier = Modifier.size(MaterialTheme.dimens.spaceXl),
                )
            }
            Box(
                modifier = Modifier
                    .size(MaterialTheme.dimens.playerControlButtonSize)
                    .clip(CircleShape)
                    .background(brush = MaterialTheme.mozicColors.accentGradient)
                    .clickable(onClick = onPlayPauseClick),
                contentAlignment = Alignment.Center,
            ) {
                if (state.isBuffering) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = stringResource(
                            if (state.isPlaying) {
                                DesignSystemR.string.action_pause
                            } else {
                                DesignSystemR.string.action_play
                            },
                        ),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(MaterialTheme.dimens.spaceXl),
                    )
                }
            }
            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = stringResource(DesignSystemR.string.cd_next),
                    modifier = Modifier.size(MaterialTheme.dimens.spaceXl),
                )
            }
        }
    }
}

/**
 * While the user drags, the thumb follows the gesture locally and
 * `seekTo` only fires on release (`onValueChangeFinished`) — seeking on every
 * drag frame would fight the controller's own 250ms position-ticking loop and
 * make the thumb stutter.
 */
@Composable
private fun PlayerSeekBar(
    positionMs: Long,
    durationMs: Long,
    onSeekFinished: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragPositionMs by remember { mutableStateOf<Float?>(null) }
    val safeDurationMs = durationMs.coerceAtLeast(1L).toFloat()
    val displayedPositionMs = dragPositionMs ?: positionMs.toFloat().coerceIn(0f, safeDurationMs)

    Column(modifier = modifier) {
        Slider(
            value = displayedPositionMs,
            onValueChange = { dragPositionMs = it },
            onValueChangeFinished = {
                // Read the live state here, not `displayedPositionMs` — a
                // plain tap fires onValueChange then onValueChangeFinished
                // within the same gesture pass, before Compose gets a chance
                // to recompose with the just-set value, so a `val` captured
                // in this closure would still hold the pre-tap position. A
                // drag spans multiple frames, so a recomposition lands in
                // between and happened to mask this for drags only.
                dragPositionMs?.let { onSeekFinished(it.toLong()) }
                dragPositionMs = null
            },
            valueRange = 0f..safeDurationMs,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.outline,
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatTrackTimeMs(displayedPositionMs.toLong()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatTrackTimeMs(durationMs),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Always Western digits regardless of locale — timestamps, not translatable text. */
private fun formatTrackTimeMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
