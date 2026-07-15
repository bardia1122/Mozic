package com.example.mozic.feature.home.component

import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.domain.model.Song
import com.example.mozic.core.ui.component.CoverImage
import kotlinx.coroutines.delay

private const val AUTO_ADVANCE_DELAY_MS = 4_000L
private const val SCALE_PEAK = 1f
private const val SCALE_TROUGH = 0.9f
private const val ALPHA_PEAK = 1f
private const val ALPHA_TROUGH = 0.6f
private const val INDICATOR_ALPHA_ACTIVE = 1f
private const val INDICATOR_ALPHA_INACTIVE = 0.35f
private const val SCRIM_ALPHA = 0.75f
private const val CAPTION_SUBTITLE_ALPHA = 0.85f

/**
 * Trending/new-albums carousel: auto-advances every 4s, resets on manual swipe
 * (keyed on `settledPage`, so a drag naturally cancels the pending advance),
 * with page indicators and a subtle scale/fade on the non-focused pages.
 */
@Composable
fun HomeCarousel(
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (songs.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { songs.size })

    LaunchedEffect(pagerState.settledPage, songs.size) {
        if (songs.size <= 1) return@LaunchedEffect
        delay(AUTO_ADVANCE_DELAY_MS)
        pagerState.animateScrollToPage((pagerState.settledPage + 1) % songs.size)
    }

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(MaterialTheme.dimens.carouselHeight),
        ) { page ->
            val song = songs[page]
            val distance = pagerState.getOffsetDistanceInPages(page).coerceIn(-1f, 1f)
            val focusFraction = 1f - (if (distance < 0f) -distance else distance)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = MaterialTheme.dimens.screenHorizontalPadding)
                    .graphicsLayer {
                        scaleX = SCALE_TROUGH + (SCALE_PEAK - SCALE_TROUGH) * focusFraction
                        scaleY = scaleX
                        alpha = ALPHA_TROUGH + (ALPHA_PEAK - ALPHA_TROUGH) * focusFraction
                    }
                    .clip(MaterialTheme.shapes.large)
                    .clickable { onSongClick(song) },
            ) {
                CoverImage(
                    model = song.coverImageUrl,
                    contentDescription = song.title,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.scrim.copy(alpha = SCRIM_ALPHA),
                                ),
                            ),
                        ),
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(MaterialTheme.dimens.spaceMd),
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = song.artistName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = CAPTION_SUBTITLE_ALPHA),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        if (songs.size > 1) {
            CarouselIndicators(
                pageCount = songs.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MaterialTheme.dimens.spaceXs),
            )
        }
    }
}

@Composable
private fun CarouselIndicators(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceXs, Alignment.CenterHorizontally),
    ) {
        repeat(pageCount) { page ->
            val active = page == currentPage
            Box(
                modifier = Modifier
                    .size(MaterialTheme.dimens.indicatorDotSize)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(
                            alpha = if (active) INDICATOR_ALPHA_ACTIVE else INDICATOR_ALPHA_INACTIVE,
                        ),
                    ),
            )
        }
    }
}
