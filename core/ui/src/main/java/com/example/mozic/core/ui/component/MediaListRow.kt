package com.example.mozic.core.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.ui.modifier.artworkPlaceholder

/**
 * Leading thumbnail + title/subtitle, single-column list row. Used for search
 * results now; Liked/Recently-played/playlist-detail song lists reuse it later
 * instead of each hand-rolling a row.
 */
@Composable
fun MediaListRow(
    imageUrl: String?,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageShape: Shape = MaterialTheme.shapes.small,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = MaterialTheme.dimens.screenHorizontalPadding,
                vertical = MaterialTheme.dimens.spaceXs,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceMd),
    ) {
        Box(
            modifier = Modifier
                .size(MaterialTheme.dimens.listRowImageSize)
                .clip(imageShape),
        ) {
            if (imageUrl != null) {
                CoverImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .artworkPlaceholder(),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        trailing?.invoke()
    }
}

/** Same footprint as [MediaListRow], shimmering, for skeleton/first-page loading. */
@Composable
fun MediaListRowSkeleton(
    modifier: Modifier = Modifier,
    imageShape: Shape = MaterialTheme.shapes.small,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = MaterialTheme.dimens.screenHorizontalPadding,
                vertical = MaterialTheme.dimens.spaceXs,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceMd),
    ) {
        ShimmerBox(
            modifier = Modifier.size(MaterialTheme.dimens.listRowImageSize),
            shape = imageShape,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceXs),
        ) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(SKELETON_TITLE_WIDTH_FRACTION)
                    .height(MaterialTheme.dimens.skeletonLineHeight),
            )
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(SKELETON_SUBTITLE_WIDTH_FRACTION)
                    .height(MaterialTheme.dimens.skeletonLineHeight),
            )
        }
    }
}

private const val SKELETON_TITLE_WIDTH_FRACTION = 0.6f
private const val SKELETON_SUBTITLE_WIDTH_FRACTION = 0.4f
