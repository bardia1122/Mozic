package com.example.mozic.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.designsystem.theme.mozicColors

/**
 * Renders a playlist's cover. Priority: [coverImageUrl] (a curated cover —
 * WORLD/LOCAL playlists have one) wins outright; otherwise a collage is
 * built from [coverImageUrls] (user-created playlists never have a curated
 * cover of their own, only their member songs' covers) — 1 song fills the
 * whole tile, 2 split left/right, 3 are two on top and one spanning the
 * bottom, 4+ are a 2x2 grid of the first 4; a playlist with no songs at all
 * falls back to [EmptyPlaylistCover].
 *
 * Takes the raw fields rather than a whole `Playlist` — `PlaylistDetailScreen`'s
 * header only has these individually (passed through its nav route, not a
 * `Playlist` instance), so this stays usable from both there and
 * [PlaylistCard] instead of forcing a throwaway `Playlist` just to call it.
 *
 * Callers size/clip via [modifier], same contract as [CoverImage] — this
 * only arranges tiles inside whatever shape/size it's given.
 */
@Composable
fun PlaylistCoverArt(
    coverImageUrl: String?,
    coverImageUrls: List<String>,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val curatedCover = coverImageUrl
    val songCovers = coverImageUrls
    when {
        curatedCover != null ->
            CoverImage(model = curatedCover, contentDescription = contentDescription, modifier = modifier)

        songCovers.isEmpty() -> EmptyPlaylistCover(modifier = modifier)

        songCovers.size == 1 ->
            CoverImage(model = songCovers[0], contentDescription = contentDescription, modifier = modifier)

        songCovers.size == 2 -> Row(modifier = modifier) {
            songCovers.forEach { url ->
                CollageTile(
                    url = url,
                    contentDescription = contentDescription,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
        }

        songCovers.size == 3 -> Column(modifier = modifier) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                songCovers.take(2).forEach { url ->
                    CollageTile(
                        url = url,
                        contentDescription = contentDescription,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            }
            CollageTile(
                url = songCovers[2],
                contentDescription = contentDescription,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
        }

        else -> Column(modifier = modifier) {
            songCovers.take(4).chunked(2).forEach { rowUrls ->
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    rowUrls.forEach { url ->
                        CollageTile(
                            url = url,
                            contentDescription = contentDescription,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                }
            }
        }
    }
}

/**
 * `ContentScale.Crop` (already [CoverImage]'s default) is what makes a
 * rectangular slice of a square cover still read as art, not a stretched sliver.
 */
@Composable
private fun CollageTile(url: String, contentDescription: String?, modifier: Modifier = Modifier) {
    CoverImage(
        model = url,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier,
    )
}

/**
 * A [MusicNote] glyph on the same neutral fill the artwork-stripe placeholder
 * uses, instead of that stripe texture — the stripe means "placeholder, real
 * image goes here" (DESIGN.md §4), which is the wrong signal for a playlist
 * that's genuinely, permanently empty until someone adds a song to it.
 */
@Composable
private fun EmptyPlaylistCover(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.MusicNote,
            contentDescription = null,
            tint = MaterialTheme.mozicColors.textTertiary,
            modifier = Modifier.size(MaterialTheme.dimens.emptyStateIconSize),
        )
    }
}
