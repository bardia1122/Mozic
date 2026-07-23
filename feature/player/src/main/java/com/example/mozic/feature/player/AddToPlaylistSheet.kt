package com.example.mozic.feature.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mozic.core.designsystem.R
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.domain.model.Playlist
import com.example.mozic.core.ui.component.PlaylistCoverArt

/**
 * Reached from Now Playing's overflow menu ("Add to playlist" — the design
 * handoff's new menu item). Same shape as `ShareSongSheet`'s friend picker:
 * a bottom sheet, its own route/`ViewModel`. [onSongAdded] — not [onDismiss]
 * — fires on success, so the caller (which owns the app-level snackbar that
 * outlives this sheet's own dismissal) can both close the sheet and show the
 * "Added to playlist X" toast; a snackbar hosted on this sheet itself
 * wouldn't survive its own dismiss, same reasoning as `MozicApp`'s
 * logged-out/create-playlist-login-required toasts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistSheet(
    onDismiss: () -> Unit,
    onSongAdded: (playlistTitle: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddToPlaylistViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AddToPlaylistEffect.Added -> onSongAdded(effect.playlistTitle)
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        Column(modifier = Modifier.padding(bottom = MaterialTheme.dimens.spaceLg)) {
            Text(
                text = stringResource(R.string.player_menu_add_to_playlist),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.dimens.screenHorizontalPadding,
                    vertical = MaterialTheme.dimens.spaceSm,
                ),
            )
            when (val state = uiState) {
                AddToPlaylistUiState.Loading -> AddToPlaylistMessage(text = stringResource(R.string.state_loading))

                AddToPlaylistUiState.LoggedOut -> AddToPlaylistMessage(
                    text = stringResource(R.string.player_add_to_playlist_login_required),
                )

                is AddToPlaylistUiState.Content -> if (state.playlists.isEmpty()) {
                    AddToPlaylistMessage(text = stringResource(R.string.player_add_to_playlist_empty_title))
                } else {
                    LazyColumn {
                        items(state.playlists, key = Playlist::id) { playlist ->
                            AddToPlaylistRow(
                                playlist = playlist,
                                isAdding = state.addingPlaylistId == playlist.id,
                                onClick = { viewModel.onPlaylistClick(playlist.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddToPlaylistMessage(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(MaterialTheme.dimens.screenHorizontalPadding)
            .height(MaterialTheme.dimens.spaceXl),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Same layout `MediaListRow` uses, but with [PlaylistCoverArt] as the leading
 * thumbnail instead of `MediaListRow`'s own single-`imageUrl` slot — a
 * user-created playlist never has a curated [Playlist.coverImageUrl], only
 * its member songs' covers ([Playlist.coverImageUrls]), so `MediaListRow`
 * alone always fell through to its generic "no image" placeholder here.
 */
@Composable
private fun AddToPlaylistRow(
    playlist: Playlist,
    isAdding: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = MaterialTheme.dimens.screenHorizontalPadding,
                vertical = MaterialTheme.dimens.spaceXs,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceMd),
    ) {
        PlaylistCoverArt(
            coverImageUrl = playlist.coverImageUrl,
            coverImageUrls = playlist.coverImageUrls,
            contentDescription = playlist.title,
            modifier = Modifier
                .size(MaterialTheme.dimens.listRowImageSize)
                .clip(MaterialTheme.shapes.small),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.home_playlist_song_count, playlist.songCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (isAdding) {
            CircularProgressIndicator(
                modifier = Modifier.size(MaterialTheme.dimens.spaceMd),
                strokeWidth = MaterialTheme.dimens.progressStrokeWidthThin,
            )
        }
    }
}
