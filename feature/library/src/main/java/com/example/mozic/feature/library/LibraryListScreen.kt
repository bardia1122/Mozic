package com.example.mozic.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mozic.core.designsystem.R as DesignSystemR
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.designsystem.theme.mozicColors
import com.example.mozic.core.domain.model.DownloadState
import com.example.mozic.core.domain.model.Song
import com.example.mozic.core.ui.component.DownloadIconButton
import com.example.mozic.core.ui.component.MediaListRow
import com.example.mozic.core.ui.component.MediaListRowSkeleton
import com.example.mozic.feature.library.navigation.LibraryListKind

private const val SKELETON_ROW_COUNT = 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryListScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val upgradeMessage = stringResource(DesignSystemR.string.premium_upsell_body)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                LibraryListEffect.ShowUpgradePrompt -> snackbarHostState.showSnackbar(upgradeMessage)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(DesignSystemR.string.action_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LibraryListContent(
            kind = viewModel.kind,
            uiState = uiState,
            onEvent = viewModel::onEvent,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        )
    }
}

@Composable
private fun LibraryListContent(
    kind: LibraryListKind,
    uiState: LibraryListUiState,
    onEvent: (LibraryListEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = uiState as? LibraryListUiState.Content
    val songs = content?.songs.orEmpty()
    val downloadStates = content?.downloadStates.orEmpty()
    val isPremium = content?.isPremium ?: false
    val queueIds = songs.map(Song::id)
    val isLoading = uiState is LibraryListUiState.Loading

    LazyColumn(modifier = modifier) {
        item {
            LibraryListHeader(
                kind = kind,
                songCount = songs.size,
                playAllEnabled = songs.isNotEmpty(),
                onPlayAll = { shuffle -> onEvent(LibraryListEvent.PlayAll(queueIds, shuffle)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.dimens.screenHorizontalPadding),
            )
        }

        when {
            isLoading -> items(SKELETON_ROW_COUNT) { MediaListRowSkeleton() }

            songs.isEmpty() -> item {
                EmptyLibraryMessage(
                    subtitle = stringResource(kind.emptySubtitleRes()),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            else -> items(songs, key = Song::id) { song ->
                SwipeableLibrarySongRow(
                    song = song,
                    onClick = { onEvent(LibraryListEvent.SongClick(song, queueIds)) },
                    onRemove = { onEvent(LibraryListEvent.RemoveSong(song.id)) },
                    modifier = Modifier.animateItem(),
                ) {
                    DownloadIconButton(
                        downloadState = downloadStates[song.id] ?: DownloadState.NotDownloaded,
                        isPremium = isPremium,
                        onDownloadClick = { onEvent(LibraryListEvent.RequestDownload(song.id)) },
                        onRemoveClick = { onEvent(LibraryListEvent.RequestRemoveDownload(song.id)) },
                        onUpgradeRequired = { onEvent(LibraryListEvent.UpgradeRequired) },
                    )
                }
            }
        }
    }
}

/**
 * A `LazyColumn` `item {}` has unbounded height along the scroll axis, so
 * (unlike [com.example.mozic.core.ui.component.PlaceholderScreen], built for
 * a whole-screen slot) this stays wrap-content instead of `fillMaxSize()`.
 */
@Composable
private fun EmptyLibraryMessage(subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = MaterialTheme.dimens.spaceXl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceXs),
    ) {
        Text(text = stringResource(DesignSystemR.string.state_empty), style = MaterialTheme.typography.headlineSmall)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LibraryListHeader(
    kind: LibraryListKind,
    songCount: Int,
    playAllEnabled: Boolean,
    onPlayAll: (shuffle: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceMd),
    ) {
        Box(
            modifier = Modifier
                .size(MaterialTheme.dimens.heroCoverSize)
                .clip(MaterialTheme.shapes.large)
                .background(brush = MaterialTheme.mozicColors.accentGradient),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = kind.icon(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(MaterialTheme.dimens.spaceXl),
            )
        }

        Text(
            text = stringResource(kind.titleRes()),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(DesignSystemR.string.home_playlist_song_count, songCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceSm)) {
            Button(
                onClick = { onPlayAll(false) },
                enabled = playAllEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = MaterialTheme.mozicColors.textTertiary,
                ),
                modifier = Modifier.background(
                    brush = MaterialTheme.mozicColors.accentGradient,
                    shape = ButtonDefaults.shape,
                ),
            ) {
                Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(MaterialTheme.dimens.spaceXs))
                Text(
                    text = stringResource(DesignSystemR.string.playlists_play_all),
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            OutlinedButton(onClick = { onPlayAll(true) }, enabled = playAllEnabled) {
                Icon(imageVector = Icons.Outlined.Shuffle, contentDescription = null)
                Spacer(Modifier.width(MaterialTheme.dimens.spaceXs))
                Text(stringResource(DesignSystemR.string.playlists_shuffle))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableLibrarySongRow(
    song: Song,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onRemove()
                true
            } else {
                false
            }
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = { RemoveSongBackdrop() },
    ) {
        MediaListRow(
            imageUrl = song.coverImageUrl,
            title = song.title,
            subtitle = song.artistName,
            onClick = onClick,
            trailing = trailing,
        )
    }
}

@Composable
private fun RemoveSongBackdrop(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.error)
            .padding(horizontal = MaterialTheme.dimens.screenHorizontalPadding),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = stringResource(DesignSystemR.string.cd_remove_song),
            tint = MaterialTheme.colorScheme.onError,
        )
    }
}

private fun LibraryListKind.titleRes(): Int = when (this) {
    LibraryListKind.LIKED -> DesignSystemR.string.library_liked_title
    LibraryListKind.RECENTLY_PLAYED -> DesignSystemR.string.library_recent_title
}

private fun LibraryListKind.emptySubtitleRes(): Int = when (this) {
    LibraryListKind.LIKED -> DesignSystemR.string.library_liked_empty
    LibraryListKind.RECENTLY_PLAYED -> DesignSystemR.string.library_recent_empty
}

private fun LibraryListKind.icon(): ImageVector = when (this) {
    LibraryListKind.LIKED -> Icons.Filled.Favorite
    LibraryListKind.RECENTLY_PLAYED -> Icons.Filled.History
}
