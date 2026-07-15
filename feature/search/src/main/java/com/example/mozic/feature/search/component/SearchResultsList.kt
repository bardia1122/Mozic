package com.example.mozic.feature.search.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.example.mozic.core.designsystem.R
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.domain.model.SearchResult
import com.example.mozic.core.ui.component.MediaListRow
import com.example.mozic.core.ui.component.MediaListRowSkeleton
import com.example.mozic.core.ui.component.PlaceholderScreen

private const val SKELETON_ROW_COUNT = 6

@Composable
fun SearchResultsList(
    pagingItems: LazyPagingItems<SearchResult>,
    onResultClick: (SearchResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isInitialLoad = pagingItems.loadState.refresh is LoadState.Loading
    val isEmpty = pagingItems.loadState.refresh is LoadState.NotLoading && pagingItems.itemCount == 0

    when {
        isInitialLoad -> LazyColumn(modifier = modifier) {
            items(SKELETON_ROW_COUNT) { MediaListRowSkeleton() }
        }

        isEmpty -> PlaceholderScreen(
            title = stringResource(R.string.search_no_results),
            subtitle = stringResource(R.string.state_empty),
            modifier = modifier,
        )

        else -> LazyColumn(modifier = modifier) {
            items(pagingItems.itemCount) { index ->
                pagingItems[index]?.let { result ->
                    SearchResultRow(result = result, onClick = { onResultClick(result) })
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

@Composable
private fun SearchResultRow(
    result: SearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (result) {
        is SearchResult.SongResult -> MediaListRow(
            imageUrl = result.song.coverImageUrl,
            title = result.song.title,
            subtitle = result.song.artistName,
            onClick = onClick,
            modifier = modifier,
        )

        is SearchResult.ArtistResult -> MediaListRow(
            imageUrl = result.artist.imageUrl,
            title = result.artist.name,
            subtitle = null,
            onClick = onClick,
            imageShape = CircleShape,
            modifier = modifier,
        )

        is SearchResult.PlaylistResult -> MediaListRow(
            imageUrl = result.playlist.coverImageUrl,
            title = result.playlist.title,
            subtitle = null,
            onClick = onClick,
            modifier = modifier,
        )
    }
}
