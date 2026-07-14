package com.example.mozic.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextOverflow
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.domain.model.Song

/**
 * Cover + title + artist, used for every song row on Home and beyond.
 * Press-scales per the spec's micro-interaction rule.
 */
@Composable
fun SongCard(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.95f else 1f, label = "songCardScale")

    Column(
        modifier = modifier
            .width(MaterialTheme.dimens.cardImageSize)
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceXs),
    ) {
        CoverImage(
            model = song.coverImageUrl,
            contentDescription = song.title,
            modifier = Modifier
                .size(MaterialTheme.dimens.cardImageSize)
                .clip(MaterialTheme.shapes.medium),
        )
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = song.artistName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Same footprint as [SongCard], shimmering, for skeleton loading. */
@Composable
fun SongCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.width(MaterialTheme.dimens.cardImageSize),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceXs),
    ) {
        ShimmerBox(
            modifier = Modifier.size(MaterialTheme.dimens.cardImageSize),
            shape = MaterialTheme.shapes.medium,
        )
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(MaterialTheme.dimens.skeletonLineHeight))
        ShimmerBox(
            modifier = Modifier
                .width(MaterialTheme.dimens.cardImageSize * 0.6f)
                .height(MaterialTheme.dimens.skeletonLineHeight),
        )
    }
}
