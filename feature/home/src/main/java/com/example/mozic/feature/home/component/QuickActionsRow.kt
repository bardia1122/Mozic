package com.example.mozic.feature.home.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.mozic.core.designsystem.R
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.feature.home.QuickAction

/** The four Home shortcuts: Liked / Recently played / My playlists / Top artists. */
@Composable
fun QuickActionsRow(
    onActionClick: (QuickAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        QuickAction.entries.forEach { action ->
            QuickActionButton(action = action, onClick = { onActionClick(action) })
        }
    }
}

@Composable
private fun QuickActionButton(
    action: QuickAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(MaterialTheme.dimens.spaceXs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceXxs),
    ) {
        Icon(
            imageVector = action.icon(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(action.labelRes()),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun QuickAction.icon(): ImageVector = when (this) {
    QuickAction.LIKED -> Icons.Filled.Favorite
    QuickAction.RECENTLY_PLAYED -> Icons.Filled.History
    QuickAction.MY_PLAYLISTS -> Icons.Filled.LibraryMusic
    QuickAction.TOP_ARTISTS -> Icons.Filled.Star
}

private fun QuickAction.labelRes(): Int = when (this) {
    QuickAction.LIKED -> R.string.home_quick_action_liked
    QuickAction.RECENTLY_PLAYED -> R.string.home_quick_action_recent
    QuickAction.MY_PLAYLISTS -> R.string.home_quick_action_playlists
    QuickAction.TOP_ARTISTS -> R.string.home_quick_action_top_artists
}
