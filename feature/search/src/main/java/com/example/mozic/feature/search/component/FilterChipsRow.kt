package com.example.mozic.feature.search.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.mozic.core.designsystem.R
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.designsystem.theme.mozicColors
import com.example.mozic.core.domain.model.SearchFilter

private val ChipShape = RoundedCornerShape(percent = 50)

@Composable
fun FilterChipsRow(
    selected: SearchFilter,
    onFilterClick: (SearchFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = MaterialTheme.dimens.screenHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceXs),
    ) {
        items(SearchFilter.entries) { filter ->
            val active = filter == selected
            FilterChip(
                selected = active,
                onClick = { onFilterClick(filter) },
                label = { Text(stringResource(filter.labelRes())) },
                shape = ChipShape,
                modifier = if (active) {
                    Modifier.background(brush = MaterialTheme.mozicColors.accentGradient, shape = ChipShape)
                } else {
                    Modifier
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.Transparent,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = Color.Transparent,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = active,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = Color.Transparent,
                    borderWidth = MaterialTheme.dimens.borderWidthHairline,
                ),
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
