package com.example.mozic.core.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.mozic.core.designsystem.R

/**
 * Opens the song-share friend picker (I1) — reused on every song row and on
 * Now Playing. Stateless, same "caller owns the meaning" shape as
 * [DownloadIconButton]/[FollowIconButton]: this button only ever calls
 * [onClick] with no state of its own, since sharing has no per-song
 * on/off toggle to reflect.
 */
@Composable
fun ShareIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.Filled.Share,
            contentDescription = stringResource(R.string.cd_share_song),
        )
    }
}
