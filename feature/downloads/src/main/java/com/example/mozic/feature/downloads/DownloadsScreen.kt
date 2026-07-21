package com.example.mozic.feature.downloads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mozic.core.designsystem.R as DesignSystemR
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.domain.model.Song
import com.example.mozic.core.ui.component.EmptyState
import com.example.mozic.core.ui.component.MediaListRow
import com.example.mozic.core.ui.component.MediaListRowSkeleton
import com.example.mozic.core.ui.component.ShareIconButton

private const val SKELETON_ROW_COUNT = 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    onShareClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DownloadsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(modifier = modifier) { innerPadding ->
        DownloadsContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onShareClick = onShareClick,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        )
    }
}

@Composable
private fun DownloadsContent(
    uiState: DownloadsUiState,
    onEvent: (DownloadsEvent) -> Unit,
    onShareClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val songs = (uiState as? DownloadsUiState.Content)?.songs.orEmpty()
    val sortOrder = (uiState as? DownloadsUiState.Content)?.sortOrder ?: DownloadSortOrder.NAME
    val queueIds = songs.map(Song::id)
    val isLoading = uiState is DownloadsUiState.Loading

    LazyColumn(modifier = modifier) {
        item {
            DownloadsHeader(
                sortOrder = sortOrder,
                onSortOrderSelected = { onEvent(DownloadsEvent.SetSortOrder(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = MaterialTheme.dimens.screenHorizontalPadding,
                        vertical = MaterialTheme.dimens.spaceSm,
                    ),
            )
        }

        when {
            isLoading -> items(SKELETON_ROW_COUNT) { MediaListRowSkeleton() }

            songs.isEmpty() -> item {
                EmptyState(
                    icon = Icons.Filled.Download,
                    title = stringResource(DesignSystemR.string.state_empty),
                    subtitle = stringResource(DesignSystemR.string.downloads_empty),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = MaterialTheme.dimens.spaceXl),
                )
            }

            else -> items(songs, key = Song::id) { song ->
                SwipeableDownloadRow(
                    song = song,
                    onClick = { onEvent(DownloadsEvent.SongClick(song, queueIds)) },
                    onRemove = { onEvent(DownloadsEvent.RemoveDownload(song.id)) },
                    onShareClick = { onShareClick(song.id) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun DownloadsHeader(
    sortOrder: DownloadSortOrder,
    onSortOrderSelected: (DownloadSortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = stringResource(DesignSystemR.string.nav_downloads), style = MaterialTheme.typography.headlineSmall)

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Filled.Sort,
                    contentDescription = stringResource(DesignSystemR.string.downloads_sort_cd),
                )
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(DesignSystemR.string.downloads_sort_name)) },
                    trailingIcon = { if (sortOrder == DownloadSortOrder.NAME) SelectedSortCheckmark() },
                    onClick = { onSortOrderSelected(DownloadSortOrder.NAME); menuExpanded = false },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(DesignSystemR.string.downloads_sort_artist)) },
                    trailingIcon = { if (sortOrder == DownloadSortOrder.ARTIST) SelectedSortCheckmark() },
                    onClick = { onSortOrderSelected(DownloadSortOrder.ARTIST); menuExpanded = false },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableDownloadRow(
    song: Song,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
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
        backgroundContent = { RemoveDownloadBackdrop() },
    ) {
        // `MediaListRow` paints no background of its own, so without an opaque
        // one here the red backdrop shows straight through at rest, not just
        // mid-swipe — same bug/fix as `LibraryListScreen`'s equivalent row.
        MediaListRow(
            imageUrl = song.coverImageUrl,
            title = song.title,
            subtitle = song.artistName,
            onClick = onClick,
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            trailing = { ShareIconButton(onClick = onShareClick) },
        )
    }
}

@Composable
private fun RemoveDownloadBackdrop(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.error)
            .padding(horizontal = MaterialTheme.dimens.screenHorizontalPadding),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = stringResource(DesignSystemR.string.cd_remove_download),
            tint = MaterialTheme.colorScheme.onError,
        )
    }
}

@Composable
private fun SelectedSortCheckmark(modifier: Modifier = Modifier) {
    Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = modifier)
}
