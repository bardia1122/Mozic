package com.example.mozic.feature.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MarkChatUnread
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mozic.core.designsystem.R
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.domain.model.chat.Conversation
import com.example.mozic.core.domain.model.chat.Message
import com.example.mozic.core.domain.model.chat.MessagePayload
import com.example.mozic.core.ui.component.CoverImage
import com.example.mozic.core.ui.component.MediaListRowSkeleton
import com.example.mozic.core.ui.component.PlaceholderScreen
import com.example.mozic.feature.chat.component.formatConversationTime

private const val SKELETON_ROW_COUNT = 6
private const val MAX_UNREAD_DIGITS = 9

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    onBackClick: () -> Unit,
    onConversationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConversationListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.chat_title)) },
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
        ConversationListContent(
            uiState = uiState,
            onConversationClick = onConversationClick,
            onEvent = viewModel::onEvent,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        )
    }
}

@Composable
private fun ConversationListContent(
    uiState: ConversationListUiState,
    onConversationClick: (String) -> Unit,
    onEvent: (ConversationListEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        ConversationListUiState.Loading -> LazyColumn(modifier = modifier) {
            items(SKELETON_ROW_COUNT) { MediaListRowSkeleton(imageShape = CircleShape) }
        }

        is ConversationListUiState.Content -> if (uiState.conversations.isEmpty()) {
            PlaceholderScreen(
                title = stringResource(R.string.chat_conversations_empty_title),
                subtitle = stringResource(R.string.chat_conversations_empty_subtitle),
                modifier = modifier,
            )
        } else {
            LazyColumn(modifier = modifier) {
                items(uiState.conversations, key = Conversation::id) { conversation ->
                    ConversationRow(
                        conversation = conversation,
                        onClick = { onConversationClick(conversation.id) },
                        onDeleteClick = { onEvent(ConversationListEvent.DeleteConversation(conversation.id)) },
                        onMarkUnreadClick = { onEvent(ConversationListEvent.MarkUnread(conversation.id)) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationRow(
    conversation: Conversation,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMarkUnreadClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = { menuExpanded = true })
                .padding(
                    horizontal = MaterialTheme.dimens.screenHorizontalPadding,
                    vertical = MaterialTheme.dimens.spaceXs,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceMd),
        ) {
            CoverImage(
                model = conversation.peer.avatarUrl,
                contentDescription = conversation.peer.displayName,
                modifier = Modifier
                    .size(MaterialTheme.dimens.listRowImageSize)
                    .clip(CircleShape),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.peer.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = conversation.lastMessage?.let { previewText(it) }.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                conversation.lastMessage?.let { last ->
                    Text(
                        text = formatConversationTime(last.sentAtEpochMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (conversation.unreadCount > 0) {
                    UnreadBadge(count = conversation.unreadCount)
                }
            }
        }

        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.chat_mark_unread)) },
                leadingIcon = { Icon(imageVector = Icons.Filled.MarkChatUnread, contentDescription = null) },
                onClick = { menuExpanded = false; onMarkUnreadClick() },
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.chat_delete_conversation),
                        color = MaterialTheme.colorScheme.error,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
                onClick = { menuExpanded = false; onDeleteClick() },
            )
        }
    }
}

@Composable
private fun UnreadBadge(count: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(top = MaterialTheme.dimens.spaceXxs)
            .size(MaterialTheme.dimens.unreadBadgeSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        val label = if (count > MAX_UNREAD_DIGITS) {
            stringResource(R.string.chat_unread_badge_overflow)
        } else {
            count.toString()
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun previewText(message: Message): String = when (val payload = message.payload) {
    is MessagePayload.Text -> payload.text
    is MessagePayload.SongShare -> stringResource(R.string.chat_message_preview_song, payload.title)
}
