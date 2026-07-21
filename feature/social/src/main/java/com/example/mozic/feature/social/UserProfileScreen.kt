package com.example.mozic.feature.social

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mozic.core.designsystem.R
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.designsystem.theme.mozicColors
import com.example.mozic.core.domain.model.Playlist
import com.example.mozic.core.domain.model.User
import com.example.mozic.core.ui.component.CoverImage
import com.example.mozic.core.ui.component.PlaylistCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onBackClick: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UserProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val followedMessage = stringResource(R.string.social_followed)
    val unfollowedMessage = stringResource(R.string.social_unfollowed)
    val actionFailedMessage = stringResource(R.string.social_action_failed)
    val loginRequiredMessage = stringResource(R.string.social_login_required)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                SocialActionEffect.Followed -> snackbarHostState.showSnackbar(followedMessage)
                SocialActionEffect.Unfollowed -> snackbarHostState.showSnackbar(unfollowedMessage)
                SocialActionEffect.ActionFailed -> snackbarHostState.showSnackbar(actionFailedMessage)
                SocialActionEffect.LoginRequired -> snackbarHostState.showSnackbar(loginRequiredMessage)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    val title = (uiState as? UserProfileUiState.Content)?.user?.displayName.orEmpty()
                    Text(title)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (val state = uiState) {
            UserProfileUiState.Loading -> Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            UserProfileUiState.NotFound -> Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = stringResource(R.string.state_error), style = MaterialTheme.typography.bodyLarge)
            }

            is UserProfileUiState.Content -> UserProfileContent(
                state = state,
                onFollowToggle = { viewModel.onFollowToggle(state.user.isFollowed) },
                onPlaylistClick = onPlaylistClick,
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
            )
        }
    }
}

@Composable
private fun UserProfileContent(
    state: UserProfileUiState.Content,
    onFollowToggle: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = MaterialTheme.dimens.screenHorizontalPadding,
            vertical = MaterialTheme.dimens.spaceMd,
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceMd),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceMd),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            ProfileHeader(user = state.user, onFollowToggle = onFollowToggle)
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = stringResource(R.string.social_public_playlists_title),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        if (state.publicPlaylists.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceXxs)) {
                    Text(
                        text = stringResource(R.string.social_public_playlists_empty_title),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(R.string.social_public_playlists_empty_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(state.publicPlaylists, key = Playlist::id) { playlist ->
                Box(modifier = Modifier.fillMaxWidth().animateItem(), contentAlignment = Alignment.Center) {
                    PlaylistCard(playlist = playlist, onClick = { onPlaylistClick(playlist) })
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(user: User, onFollowToggle: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(bottom = MaterialTheme.dimens.spaceMd),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceXs),
    ) {
        Box(
            modifier = Modifier.size(MaterialTheme.dimens.avatarSize).clip(CircleShape),
        ) {
            if (user.avatarUrl != null) {
                CoverImage(
                    model = user.avatarUrl,
                    contentDescription = user.displayName,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(brush = MaterialTheme.mozicColors.accentGradient),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = user.displayName,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(MaterialTheme.dimens.spaceXl),
                    )
                }
            }
        }

        Text(text = user.displayName, style = MaterialTheme.typography.titleLarge)
        Text(
            text = "@${user.username}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (user.isPremium) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceXxs),
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(brush = MaterialTheme.mozicColors.accentGradient)
                    .padding(horizontal = MaterialTheme.dimens.spaceSm, vertical = MaterialTheme.dimens.spaceXxs),
            ) {
                Icon(
                    imageVector = Icons.Filled.WorkspacePremium,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(MaterialTheme.dimens.spaceMd),
                )
                Text(
                    text = stringResource(R.string.profile_premium_badge),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        Button(
            onClick = onFollowToggle,
            colors = if (user.isFollowed) {
                ButtonDefaults.outlinedButtonColors()
            } else {
                ButtonDefaults.buttonColors()
            },
            modifier = Modifier.padding(top = MaterialTheme.dimens.spaceXs),
        ) {
            Icon(
                imageVector = if (user.isFollowed) Icons.Filled.PersonRemove else Icons.Filled.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(MaterialTheme.dimens.spaceMd),
            )
            Text(
                text = stringResource(if (user.isFollowed) R.string.cd_unfollow else R.string.cd_follow),
                modifier = Modifier.padding(start = MaterialTheme.dimens.spaceXs),
            )
        }
    }
}
