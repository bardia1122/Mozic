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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MarkChatUnread
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mozic.core.designsystem.R
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.designsystem.theme.mozicColors
import com.example.mozic.core.domain.model.chat.Conversation
import com.example.mozic.core.domain.model.chat.Message
import com.example.mozic.core.domain.model.chat.MessagePayload
import com.example.mozic.core.ui.component.CoverImage
import com.example.mozic.core.ui.component.EmptyState
import com.example.mozic.core.ui.component.MediaListRowSkeleton
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
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()

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
                actions = {
                    if (uiState is ConversationListUiState.Content) {
                        IconButton(onClick = { viewModel.onEvent(ConversationListEvent.LogOut) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = stringResource(R.string.chat_log_out),
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        ConversationListContent(
            uiState = uiState,
            loginState = loginState,
            onConversationClick = onConversationClick,
            onEvent = viewModel::onEvent,
            onLoginEvent = viewModel::onLoginEvent,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        )
    }
}

@Composable
private fun ConversationListContent(
    uiState: ConversationListUiState,
    loginState: LoginFormState,
    onConversationClick: (String) -> Unit,
    onEvent: (ConversationListEvent) -> Unit,
    onLoginEvent: (LoginEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        ConversationListUiState.Loading -> LazyColumn(modifier = modifier) {
            items(SKELETON_ROW_COUNT) { MediaListRowSkeleton(imageShape = CircleShape) }
        }

        ConversationListUiState.LoggedOut -> ChatLoginForm(
            state = loginState,
            onEvent = onLoginEvent,
            modifier = modifier,
        )

        is ConversationListUiState.Content -> if (uiState.conversations.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.ChatBubbleOutline,
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

/**
 * C5: chat is the only feature gated behind a real login — every other tab
 * stays reachable unauthenticated. Demo hint is shown directly in the UI
 * since there's no "forgot password"/sign-up flow, just the 6 seeded
 * Supabase accounts (`backend/README.md`), all sharing `password123`.
 */
@Composable
private fun ChatLoginForm(
    state: LoginFormState,
    onEvent: (LoginEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.dimens.screenHorizontalPadding),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = stringResource(R.string.chat_login_title), style = MaterialTheme.typography.headlineSmall)
        Text(
            text = stringResource(R.string.chat_login_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = MaterialTheme.dimens.spaceXxs, bottom = MaterialTheme.dimens.spaceLg),
        )

        OutlinedTextField(
            value = state.email,
            onValueChange = { onEvent(LoginEvent.EmailChanged(it)) },
            label = { Text(stringResource(R.string.chat_login_email)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
        )
        OutlinedTextField(
            value = state.password,
            onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
            label = { Text(stringResource(R.string.chat_login_password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MaterialTheme.dimens.spaceSm),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
        )

        if (state.hasError) {
            Text(
                text = stringResource(R.string.chat_login_error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = MaterialTheme.dimens.spaceSm),
            )
        }

        Button(
            onClick = { onEvent(LoginEvent.Submit) },
            enabled = !state.isLoading && state.email.isNotBlank() && state.password.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MaterialTheme.dimens.spaceLg)
                .background(brush = MaterialTheme.mozicColors.accentGradient, shape = ButtonDefaults.shape),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(MaterialTheme.dimens.spaceMd),
                    strokeWidth = MaterialTheme.dimens.progressStrokeWidthThin,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(stringResource(R.string.chat_login_submit))
            }
        }

        Text(
            text = stringResource(R.string.chat_login_demo_hint),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = MaterialTheme.dimens.spaceMd),
        )
    }
}
