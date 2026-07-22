package com.example.mozic.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.mozic.R
import com.example.mozic.core.designsystem.R as DesignSystemR
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.ui.component.Avatar

/**
 * Logo + app name at start; social/chat/gear/avatar trail at the end, avatar last (flush to the
 * end edge — opens Profile, which is no longer a bottom-nav tab). No notifications bell — it had
 * no real functionality (was just a "coming soon" snackbar) and was removed rather than kept as
 * a dead button. Mirrors correctly in RTL for free via [TopAppBar].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MozicTopBar(
    avatarUrl: String?,
    onAvatarClick: () -> Unit,
    onSocialClick: () -> Unit,
    onChatClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceXs),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(MaterialTheme.dimens.spaceLg),
                )
                Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
            }
        },
        actions = {
            IconButton(onClick = onSocialClick) {
                Icon(
                    imageVector = Icons.Filled.PersonSearch,
                    contentDescription = stringResource(DesignSystemR.string.cd_open_social),
                )
            }
            IconButton(onClick = onChatClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = stringResource(DesignSystemR.string.cd_open_chat),
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(DesignSystemR.string.settings_title),
                )
            }
            IconButton(onClick = onAvatarClick) {
                Avatar(
                    model = avatarUrl,
                    contentDescription = stringResource(DesignSystemR.string.nav_profile),
                    modifier = Modifier
                        .size(MaterialTheme.dimens.topBarAvatarSize)
                        .clip(CircleShape),
                )
            }
        },
    )
}
