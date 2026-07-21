package com.example.mozic.core.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.mozic.core.designsystem.R

/**
 * Follow/unfollow toggle, reused wherever a [com.example.mozic.core.domain.model.User]
 * row appears (`:feature:social`'s search results, profile, following list —
 * C6). Stateless: the caller owns what a tap means (optimistic toggle +
 * revert-on-failure lives in the ViewModel/repository, same split as
 * [DownloadIconButton]).
 */
@Composable
fun FollowIconButton(
    isFollowed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        if (isFollowed) {
            Icon(
                imageVector = Icons.Filled.PersonRemove,
                contentDescription = stringResource(R.string.cd_unfollow),
            )
        } else {
            Icon(
                imageVector = Icons.Filled.PersonAdd,
                contentDescription = stringResource(R.string.cd_follow),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
