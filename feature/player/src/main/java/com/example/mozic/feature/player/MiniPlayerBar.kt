package com.example.mozic.feature.player

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mozic.core.designsystem.R as DesignSystemR
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.designsystem.theme.mozicColors
import com.example.mozic.core.domain.model.PlayerState
import com.example.mozic.core.domain.model.Song
import com.example.mozic.core.ui.animation.LocalMiniPlayerAnimatedVisibilityScope
import com.example.mozic.core.ui.animation.LocalSharedTransitionScope
import com.example.mozic.core.ui.component.CoverImage

/**
 * Floats above the bottom nav in `:app`'s `miniPlayer` slot. Renders nothing
 * (zero height) until a song actually starts playing — there is no "nothing
 * playing" placeholder state here, unlike the old stub this replaces.
 */
@Composable
fun MiniPlayerBar(
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val song = state.currentSong ?: return

    MiniPlayerBarContent(
        song = song,
        state = state,
        onExpand = onExpand,
        onPlayPauseClick = viewModel::togglePlayPause,
        onCloseClick = viewModel::stop,
        modifier = modifier,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun MiniPlayerBarContent(
    song: Song,
    state: PlayerState,
    onExpand: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = if (state.durationMs > 0) {
        (state.positionMs.toFloat() / state.durationMs).coerceIn(0f, 1f)
    } else {
        0f
    }

    // Shared-element anchor for the mini-player -> full-player cover morph
    // (A5) — paired with NowPlayingScreen's `RotatingCover` via the same key.
    // Only wired when both halves of the pairing (the enclosing
    // SharedTransitionScope from :app, and this composable's own
    // AnimatedVisibilityScope, also from :app) are actually present.
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val miniPlayerAnimatedVisibilityScope = LocalMiniPlayerAnimatedVisibilityScope.current
    var coverModifier: Modifier = Modifier.size(MaterialTheme.dimens.listRowImageSize)
    if (sharedTransitionScope != null && miniPlayerAnimatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            coverModifier = coverModifier.sharedElement(
                rememberSharedContentState(key = playerCoverSharedElementKey(song.id)),
                animatedVisibilityScope = miniPlayerAnimatedVisibilityScope,
            )
        }
    }
    coverModifier = coverModifier.clip(CircleShape)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onExpand),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MaterialTheme.dimens.miniPlayerProgressHeight)
                    .background(MaterialTheme.colorScheme.outline),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(MaterialTheme.mozicColors.accentGradient),
                )
            }
            Row(
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.dimens.spaceMd,
                    vertical = MaterialTheme.dimens.spaceXs,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = coverModifier) {
                    CoverImage(
                        model = song.coverImageUrl,
                        contentDescription = song.title,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Spacer(Modifier.width(MaterialTheme.dimens.spaceSm))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = song.artistName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.width(MaterialTheme.dimens.spaceSm))
                if (state.isBuffering) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(MaterialTheme.dimens.spaceSm)
                            .size(MaterialTheme.dimens.spaceLg),
                        strokeWidth = MaterialTheme.dimens.spaceXxs,
                    )
                } else {
                    IconButton(onClick = onPlayPauseClick) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = stringResource(
                                if (state.isPlaying) {
                                    DesignSystemR.string.action_pause
                                } else {
                                    DesignSystemR.string.action_play
                                },
                            ),
                        )
                    }
                }
                IconButton(onClick = onCloseClick) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(DesignSystemR.string.cd_close_mini_player),
                    )
                }
            }
        }
    }
}
