package com.example.mozic.feature.playlists

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
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.mozic.core.designsystem.R as DesignSystemR
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.designsystem.theme.mozicColors
import com.example.mozic.core.ui.component.EmptyState
import com.example.mozic.core.ui.component.MediaListRow
import com.example.mozic.core.ui.component.MediaListRowSkeleton
import com.example.mozic.core.ui.component.PlaylistCoverArt
import com.example.mozic.core.ui.component.ShareIconButton

private const val SKELETON_ROW_COUNT = 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    onBackClick: () -> Unit,
    onShareClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistDetailViewModel = hiltViewModel(),
) {
    val pagingItems = viewModel.songs.collectAsLazyPagingItems()
    // loadState.refresh flips to NotLoading a beat before itemCount actually
    // catches up to the full page (Paging's diff application isn't atomic with
    // the loadState update) — gating on loadState alone briefly showed a
    // partially-populated list (a handful of real rows, then a jump to the
    // rest) instead of the skeleton straight through to the real thing.
    val isInitialLoad = pagingItems.loadState.refresh is LoadState.Loading ||
        pagingItems.itemCount < viewModel.songCount
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
                    coverImageUrls = viewModel.coverImageUrls,
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
            } else if (pagingItems.itemCount == 0) {
                item {
                    EmptyState(
                        icon = Icons.Filled.MusicNote,
                        title = stringResource(DesignSystemR.string.playlist_detail_empty_title),
                        subtitle = stringResource(DesignSystemR.string.playlist_detail_empty_subtitle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = MaterialTheme.dimens.spaceXl),
                    )
                }
            } else {
                items(pagingItems.itemCount) { index ->
                    pagingItems[index]?.let { song ->
                        MediaListRow(
                            imageUrl = song.coverImageUrl,
                            title = song.title,
                            subtitle = song.artistName,
                            onClick = { viewModel.onEvent(PlaylistDetailEvent.SongClick(song, queueIds)) },
                            trailing = { ShareIconButton(onClick = { onShareClick(song.id) }) },
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
    coverImageUrls: List<String>,
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
        PlaylistCoverArt(
            coverImageUrl = coverImageUrl,
            coverImageUrls = coverImageUrls,
            contentDescription = title,
            modifier = Modifier
                .size(MaterialTheme.dimens.heroCoverSize)
                .clip(MaterialTheme.shapes.large),
        )

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
