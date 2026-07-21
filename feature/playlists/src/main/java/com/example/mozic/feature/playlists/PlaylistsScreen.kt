package com.example.mozic.feature.playlists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mozic.core.designsystem.R as DesignSystemR
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.domain.model.Playlist
import com.example.mozic.core.ui.component.EmptyState
import com.example.mozic.core.ui.component.PlaylistCard
import com.example.mozic.core.ui.component.PlaylistCardSkeleton

// Matches SampleData's current 2-per-category count. A real backend's counts
// will vary, but for the fakes this avoids the skeleton→content swap shrinking
// the grid (12 skeleton cards → 6 real ones) and forcing LazyVerticalGrid's
// built-in "don't leave empty space below the last item" re-anchoring — the
// same underlying mechanism as the nav-transition scroll glitch fixed in
// MozicApp.kt, just triggered by the data swap instead of navigation.
private const val SKELETON_ITEMS_PER_SECTION = 2

@Composable
fun PlaylistsScreen(
    onNavigateToDetail: (Playlist) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val gridState = rememberLazyGridState()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PlaylistsEffect.NavigateToDetail -> onNavigateToDetail(effect.playlist)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        PlaylistsContent(
            uiState = uiState,
            gridState = gridState,
            onPlaylistClick = { viewModel.onEvent(PlaylistsEvent.PlaylistClick(it)) },
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        )
    }
}

@Composable
private fun PlaylistsContent(
    uiState: PlaylistsUiState,
    gridState: LazyGridState,
    onPlaylistClick: (Playlist) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState is PlaylistsUiState.Error) {
        EmptyState(
            icon = Icons.Filled.WifiOff,
            title = stringResource(DesignSystemR.string.state_error),
            subtitle = stringResource(DesignSystemR.string.state_error_subtitle),
            modifier = modifier,
        )
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = MaterialTheme.dimens.screenHorizontalPadding,
            vertical = MaterialTheme.dimens.spaceMd,
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceMd),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceMd),
    ) {
        when (uiState) {
            PlaylistsUiState.Loading -> {
                skeletonSection(DesignSystemR.string.playlists_section_world)
                skeletonSection(DesignSystemR.string.playlists_section_local)
                skeletonSection(DesignSystemR.string.home_quick_action_playlists)
            }

            is PlaylistsUiState.Content -> {
                playlistSection(DesignSystemR.string.playlists_section_world, uiState.world, onPlaylistClick)
                playlistSection(DesignSystemR.string.playlists_section_local, uiState.local, onPlaylistClick)
                playlistSection(DesignSystemR.string.home_quick_action_playlists, uiState.mine, onPlaylistClick)
            }

            PlaylistsUiState.Error -> Unit
        }
    }
}

private fun LazyGridScope.playlistSection(
    titleRes: Int,
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit,
) {
    if (playlists.isEmpty()) return
    item(span = { GridItemSpan(maxLineSpan) }) {
        SectionHeader(titleRes)
    }
    items(playlists, key = { it.id }) { playlist ->
        Box(
            modifier = Modifier.fillMaxWidth().animateItem(),
            contentAlignment = Alignment.Center,
        ) {
            PlaylistCard(playlist = playlist, onClick = { onPlaylistClick(playlist) })
        }
    }
}

private fun LazyGridScope.skeletonSection(titleRes: Int) {
    item(span = { GridItemSpan(maxLineSpan) }) {
        SectionHeader(titleRes)
    }
    items(SKELETON_ITEMS_PER_SECTION) {
        Box(
            modifier = Modifier.fillMaxWidth().animateItem(),
            contentAlignment = Alignment.Center,
        ) {
            PlaylistCardSkeleton()
        }
    }
}

@Composable
private fun SectionHeader(titleRes: Int) {
    Text(text = stringResource(titleRes), style = MaterialTheme.typography.titleMedium)
}
