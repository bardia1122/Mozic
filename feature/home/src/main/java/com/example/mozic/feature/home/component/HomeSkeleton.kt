package com.example.mozic.feature.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.ui.component.ShimmerBox
import com.example.mozic.core.ui.component.SongCardSkeleton

private const val SKELETON_ITEMS_PER_ROW = 4
private const val SKELETON_TITLE_WIDTH_FRACTION = 0.4f

/** Same footprint as [HomeCarousel], shimmering, while Home is loading. */
@Composable
fun HomeCarouselSkeleton(modifier: Modifier = Modifier) {
    ShimmerBox(
        modifier = modifier
            .fillMaxWidth()
            .height(MaterialTheme.dimens.carouselHeight),
        shape = MaterialTheme.shapes.large,
    )
}

/** Same footprint as [HomeSectionRow]: a title bar plus a row of [SongCardSkeleton]s. */
@Composable
fun HomeSectionRowSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        ShimmerBox(
            modifier = Modifier
                .padding(horizontal = MaterialTheme.dimens.spaceMd)
                .fillMaxWidth(SKELETON_TITLE_WIDTH_FRACTION)
                .height(MaterialTheme.dimens.skeletonLineHeight),
        )
        LazyRow(
            modifier = Modifier.padding(top = MaterialTheme.dimens.spaceXs),
            contentPadding = PaddingValues(horizontal = MaterialTheme.dimens.spaceMd),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceMd),
        ) {
            items(SKELETON_ITEMS_PER_ROW) {
                SongCardSkeleton()
            }
        }
    }
}
