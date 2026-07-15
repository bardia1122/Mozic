package com.example.mozic.feature.search.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.mozic.core.designsystem.R
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.domain.model.SearchFilter

@Composable
fun FilterChipsRow(
    selected: SearchFilter,
    onFilterClick: (SearchFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = MaterialTheme.dimens.spaceMd),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceXs),
    ) {
        items(SearchFilter.entries) { filter ->
            FilterChip(
                selected = filter == selected,
                onClick = { onFilterClick(filter) },
                label = { Text(stringResource(filter.labelRes())) },
            )
        }
    }
}

private fun SearchFilter.labelRes(): Int = when (this) {
    SearchFilter.ALL -> R.string.search_filter_all
    SearchFilter.SONG -> R.string.search_filter_song
    SearchFilter.ARTIST -> R.string.search_filter_artist
    SearchFilter.PLAYLIST -> R.string.search_filter_playlist
}
