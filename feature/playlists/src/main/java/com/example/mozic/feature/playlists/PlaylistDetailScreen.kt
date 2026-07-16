package com.example.mozic.feature.playlists

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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.mozic.core.designsystem.R as DesignSystemR
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.ui.component.CoverImage
import com.example.mozic.core.ui.component.MediaListRow
import com.example.mozic.core.ui.component.MediaListRowSkeleton
import com.example.mozic.core.ui.modifier.artworkPlaceholder

private const val SKELETON_ROW_COUNT = 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistDetailViewModel = hiltViewModel(),
) {
    val pagingItems = viewModel.songs.collectAsLazyPagingItems()
    val isInitialLoad = pagingItems.loadState.refresh is LoadState.Loading
    val queueIds = remember(pagingItems.itemCount) {
        pagingItems.itemSnapshotList.items.map { it.id }
    }

    Scaffold(
        modifier = modifier,
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
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            item {
                PlaylistDetailHeader(
                    title = viewModel.title,
                    coverImageUrl = viewModel.coverImageUrl,
                    songCount = viewModel.songCount,
                    playAllEnabled = queueIds.isNotEmpty(),
                    onPlayAll = { shuffle -> viewModel.onEvent(PlaylistDetailEvent.PlayAll(queueIds, shuffle)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.dimens.screenHorizontalPadding),
                )
            }

            if (isInitialLoad) {
                items(SKELETON_ROW_COUNT) { MediaListRowSkeleton() }
            } else {
                items(pagingItems.itemCount) { index ->
                    pagingItems[index]?.let { song ->
                        MediaListRow(
                            imageUrl = song.coverImageUrl,
                            title = song.title,
                            subtitle = song.artistName,
                            onClick = { viewModel.onEvent(PlaylistDetailEvent.SongClick(song, queueIds)) },
                        )
                    }
                }
                if (pagingItems.loadState.append is LoadState.Loading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(MaterialTheme.dimens.spaceMd),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistDetailHeader(
    title: String,
    coverImageUrl: String?,
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
        if (coverImageUrl != null) {
            CoverImage(
                model = coverImageUrl,
                contentDescription = title,
                modifier = Modifier
                    .size(MaterialTheme.dimens.heroCoverSize)
                    .clip(MaterialTheme.shapes.large),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(MaterialTheme.dimens.heroCoverSize)
                    .clip(MaterialTheme.shapes.large)
                    .artworkPlaceholder(),
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(DesignSystemR.string.home_playlist_song_count, songCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceSm)) {
            Button(onClick = { onPlayAll(false) }, enabled = playAllEnabled) {
                Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(MaterialTheme.dimens.spaceXs))
                Text(stringResource(DesignSystemR.string.playlists_play_all))
            }
            OutlinedButton(onClick = { onPlayAll(true) }, enabled = playAllEnabled) {
                Icon(imageVector = Icons.Outlined.Shuffle, contentDescription = null)
                Spacer(Modifier.width(MaterialTheme.dimens.spaceXs))
                Text(stringResource(DesignSystemR.string.playlists_shuffle))
            }
        }
    }
}
