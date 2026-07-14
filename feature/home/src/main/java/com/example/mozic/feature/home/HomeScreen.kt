package com.example.mozic.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mozic.core.designsystem.R as DesignSystemR
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.domain.model.HomeRow
import com.example.mozic.core.domain.model.HomeSection
import com.example.mozic.core.domain.model.PlaylistCategory
import com.example.mozic.core.domain.model.Song
import com.example.mozic.core.ui.component.PlaceholderScreen
import com.example.mozic.core.ui.component.PlaylistCard
import com.example.mozic.core.ui.component.SongCard
import com.example.mozic.feature.home.component.HomeCarousel
import com.example.mozic.feature.home.component.HomeCarouselSkeleton
import com.example.mozic.feature.home.component.HomeSectionRow
import com.example.mozic.feature.home.component.HomeSectionRowSkeleton
import com.example.mozic.feature.home.component.QuickActionsRow

private const val SKELETON_ROW_COUNT = 3

@Composable
fun HomeScreen(
    onNavigateToPlaylists: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val comingSoonMessage = stringResource(DesignSystemR.string.placeholder_coming_soon)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                HomeEffect.NavigateToPlaylists -> onNavigateToPlaylists()
                HomeEffect.ShowComingSoon -> snackbarHostState.showSnackbar(comingSoonMessage)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        HomeContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        )
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState is HomeUiState.Error) {
        PlaceholderScreen(
            title = stringResource(DesignSystemR.string.state_error),
            subtitle = stringResource(DesignSystemR.string.nav_home),
            modifier = modifier,
        )
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = MaterialTheme.dimens.spaceMd),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceLg),
    ) {
        item {
            when (uiState) {
                is HomeUiState.Content -> HomeCarousel(
                    songs = uiState.carousel,
                    onSongClick = { song -> onEvent(HomeEvent.SongClick(song, uiState.carousel)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.dimens.spaceMd),
                )

                HomeUiState.Loading -> HomeCarouselSkeleton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.dimens.spaceMd),
                )

                HomeUiState.Error -> Unit
            }
        }

        item {
            QuickActionsRow(
                onActionClick = { action -> onEvent(HomeEvent.QuickActionClick(action)) },
                modifier = Modifier.padding(horizontal = MaterialTheme.dimens.spaceMd),
            )
        }

        when (uiState) {
            HomeUiState.Loading -> items(SKELETON_ROW_COUNT) {
                HomeSectionRowSkeleton()
            }

            is HomeUiState.Content -> items(uiState.rows) { row ->
                HomeRowContent(row = row, onEvent = onEvent)
            }

            HomeUiState.Error -> Unit
        }
    }
}

@Composable
private fun HomeRowContent(
    row: HomeRow,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (row) {
        is HomeRow.Songs -> HomeSectionRow(
            title = stringResource(row.section.titleRes()),
            modifier = modifier,
        ) {
            items(row.songs, key = Song::id) { song ->
                SongCard(
                    song = song,
                    onClick = { onEvent(HomeEvent.SongClick(song, row.songs)) },
                )
            }
        }

        is HomeRow.Playlists -> HomeSectionRow(
            title = stringResource(row.category.titleRes()),
            modifier = modifier,
        ) {
            items(row.playlists, key = { it.id }) { playlist ->
                PlaylistCard(
                    playlist = playlist,
                    onClick = { onEvent(HomeEvent.PlaylistClick(playlist)) },
                )
            }
        }
    }
}

private fun HomeSection.titleRes(): Int = when (this) {
    HomeSection.MOST_POPULAR -> DesignSystemR.string.home_section_most_popular
    HomeSection.NEWEST -> DesignSystemR.string.home_section_newest
}

private fun PlaylistCategory.titleRes(): Int = when (this) {
    PlaylistCategory.WORLD -> DesignSystemR.string.home_section_global_playlists
    PlaylistCategory.LOCAL -> DesignSystemR.string.home_section_local_playlists
    PlaylistCategory.USER -> DesignSystemR.string.home_quick_action_playlists
}
