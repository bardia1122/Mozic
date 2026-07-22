package com.example.mozic.feature.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mozic.core.designsystem.R
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.domain.model.User
import com.example.mozic.core.ui.component.MediaListRow

/**
 * I1's "friend picker" — a bottom sheet listing people the caller follows;
 * tapping one resolves/creates the DM (see `RealChatRepository.conversationWith`)
 * and sends the song, then dismisses. Reachable from any song row or Now
 * Playing, so it lives on its own [com.example.mozic.feature.chat.navigation.ShareSongRoute]
 * rather than requiring the caller to already be inside chat.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSongSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ShareSongViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ShareSongEffect.Sent -> onDismiss()
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        Column(modifier = Modifier.padding(bottom = MaterialTheme.dimens.spaceLg)) {
            Text(
                text = stringResource(R.string.chat_share_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.dimens.screenHorizontalPadding,
                    vertical = MaterialTheme.dimens.spaceSm,
                ),
            )
            when (val state = uiState) {
                ShareSongUiState.Loading -> ShareSheetMessage(text = stringResource(R.string.state_loading))

                ShareSongUiState.LoggedOut -> ShareSheetMessage(
                    text = stringResource(R.string.chat_share_login_required),
                )

                is ShareSongUiState.Content -> if (state.friends.isEmpty()) {
                    ShareSheetMessage(text = stringResource(R.string.chat_share_empty))
                } else {
                    LazyColumn {
                        items(state.friends, key = User::id) { friend ->
                            ShareFriendRow(
                                friend = friend,
                                isSending = state.sendingPeerId == friend.id,
                                onClick = { viewModel.onFriendClick(friend.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareSheetMessage(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(MaterialTheme.dimens.screenHorizontalPadding)
            .height(MaterialTheme.dimens.spaceXl),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ShareFriendRow(
    friend: User,
    isSending: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MediaListRow(
        imageUrl = friend.avatarUrl,
        title = friend.displayName,
        subtitle = "@${friend.username}",
        onClick = onClick,
        imageShape = CircleShape,
        isAvatar = true,
        modifier = modifier,
        trailing = {
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(MaterialTheme.dimens.spaceMd),
                    strokeWidth = MaterialTheme.dimens.progressStrokeWidthThin,
                )
            }
        },
    )
}
