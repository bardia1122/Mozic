package com.example.mozic.feature.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.mozic.core.designsystem.R
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.domain.model.chat.ConnectionState
import com.example.mozic.core.ui.component.CoverImage
import com.example.mozic.feature.chat.component.ChatInputBar
import com.example.mozic.feature.chat.component.MessagesList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatThreadScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatThreadViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messages = viewModel.messages.collectAsLazyPagingItems()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { ChatThreadTitle(uiState = uiState) },
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding(),
        ) {
            MessagesList(
                pagingItems = messages,
                peerId = uiState.peer?.id,
                onSongShareClick = { songId -> viewModel.onEvent(ChatThreadEvent.SongShareClick(songId)) },
                modifier = Modifier.weight(1f),
            )
            ChatInputBar(
                text = uiState.inputText,
                onTextChange = { text -> viewModel.onEvent(ChatThreadEvent.InputChanged(text)) },
                onSend = { viewModel.onEvent(ChatThreadEvent.Send) },
            )
        }
    }
}

@Composable
private fun ChatThreadTitle(uiState: ChatThreadUiState, modifier: Modifier = Modifier) {
    val peer = uiState.peer ?: return
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceXs),
    ) {
        CoverImage(
            model = peer.avatarUrl,
            contentDescription = peer.displayName,
            modifier = Modifier
                .size(MaterialTheme.dimens.listRowImageSize)
                .clip(CircleShape),
        )
        Column {
            Text(
                text = peer.displayName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val statusText = chatThreadStatusText(uiState)
            if (statusText != null) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun chatThreadStatusText(uiState: ChatThreadUiState): String? = when {
    uiState.isPeerTyping -> stringResource(R.string.chat_peer_typing)
    uiState.connectionState == ConnectionState.CONNECTING -> stringResource(R.string.chat_connection_connecting)
    uiState.connectionState == ConnectionState.OFFLINE -> stringResource(R.string.chat_connection_offline)
    else -> null
}
