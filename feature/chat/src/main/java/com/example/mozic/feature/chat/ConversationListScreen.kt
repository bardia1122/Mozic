package com.example.mozic.feature.chat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mozic.core.designsystem.R
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.designsystem.theme.mozicColors
import com.example.mozic.core.domain.model.chat.Conversation
import com.example.mozic.core.domain.model.chat.Message
import com.example.mozic.core.domain.model.chat.MessagePayload
import com.example.mozic.core.ui.component.Avatar
import com.example.mozic.core.ui.component.EmptyState
import com.example.mozic.core.ui.component.MediaListRowSkeleton
import com.example.mozic.feature.chat.component.formatConversationTime

private const val SKELETON_ROW_COUNT = 6
private const val MAX_UNREAD_DIGITS = 9
private const val AVATAR_BADGE_SIZE_DP = 28

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
                title = {
                    val titleRes = if (uiState is ConversationListUiState.LoggedOut) {
                        R.string.chat_sign_in_title
                    } else {
                        R.string.chat_title
                    }
                    Text(stringResource(titleRes))
                },
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
            Avatar(
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
 * stays reachable unauthenticated. Doubles as the sign-up form ([state]'s
 * [AuthFormMode] toggles which copy/submit action is shown) so a new user
 * can create a Supabase account without leaving the chat tab.
 *
 * `.imePadding()` MUST come before `.verticalScroll()` here, not after —
 * confirmed on-device with the two orderings swapped. With `verticalScroll`
 * outermost, its own clip/viewport bounds never shrink when the IME opens
 * (only `imePadding`'s *content* padding grows inside it), so its internal
 * "bring focused child into view" responder always computes the field as
 * already on-screen against the pre-keyboard viewport height and never
 * scrolls, even though [AuthTextField] is already requesting it. Nesting
 * `verticalScroll` *inside* `imePadding` instead makes the scrollable
 * viewport itself shrink for the keyboard, so the same responder correctly
 * detects the field is covered and scrolls it into view.
 */
@Composable
private fun ChatLoginForm(
    state: LoginFormState,
    onEvent: (LoginEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSignUp = state.mode == AuthFormMode.SIGN_UP
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(MaterialTheme.dimens.screenHorizontalPadding),
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(Modifier.height(MaterialTheme.dimens.spaceXl))
        AuthFormHeader(isSignUp)
        AuthFormFields(state = state, isSignUp = isSignUp, onEvent = onEvent)
        AuthFormMessages(state = state, isSignUp = isSignUp)
        AuthSubmitButton(
            state = state,
            isSignUp = isSignUp,
            enabled = !state.isLoading,
            onEvent = onEvent,
        )

        TextButton(
            onClick = { onEvent(LoginEvent.ToggleMode) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MaterialTheme.dimens.spaceSm),
        ) {
            Text(
                stringResource(
                    if (isSignUp) R.string.chat_signup_toggle_to_login else R.string.chat_login_toggle_to_signup,
                ),
            )
        }
    }
}

@Composable
private fun AuthFormHeader(isSignUp: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(if (isSignUp) R.string.chat_signup_title else R.string.chat_login_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(if (isSignUp) R.string.chat_signup_subtitle else R.string.chat_login_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = MaterialTheme.dimens.spaceXxs, bottom = MaterialTheme.dimens.spaceLg),
        )
    }
}

@Composable
private fun ColumnScope.AuthFormFields(
    state: LoginFormState,
    isSignUp: Boolean,
    onEvent: (LoginEvent) -> Unit,
) {
    if (isSignUp) {
        SignUpAvatarPicker(
            avatarPreviewUri = state.avatarPreviewUri,
            onPicked = onEvent,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = MaterialTheme.dimens.spaceMd),
        )
        AuthTextField(
            value = state.displayName,
            onValueChange = { onEvent(LoginEvent.DisplayNameChanged(it)) },
            label = stringResource(R.string.chat_signup_display_name),
        )
        AuthTextField(
            value = state.username,
            onValueChange = { onEvent(LoginEvent.UsernameChanged(it)) },
            label = stringResource(R.string.chat_signup_username),
            isError = state.fieldError == LoginFieldError.INVALID_USERNAME,
        )
    }

    AuthTextField(
        value = state.email,
        onValueChange = { onEvent(LoginEvent.EmailChanged(it)) },
        label = stringResource(R.string.chat_login_email),
        isError = state.fieldError == LoginFieldError.INVALID_EMAIL,
        keyboardType = KeyboardType.Email,
        topPadding = if (isSignUp) MaterialTheme.dimens.spaceSm else 0.dp,
    )
    AuthTextField(
        value = state.password,
        onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
        label = stringResource(R.string.chat_login_password),
        isError = state.fieldError == LoginFieldError.WEAK_PASSWORD ||
            state.fieldError == LoginFieldError.PASSWORD_MISMATCH,
        isPassword = true,
    )
    if (isSignUp) {
        AuthTextField(
            value = state.confirmPassword,
            onValueChange = { onEvent(LoginEvent.ConfirmPasswordChanged(it)) },
            label = stringResource(R.string.chat_signup_confirm_password),
            isError = state.fieldError == LoginFieldError.PASSWORD_MISMATCH,
            isPassword = true,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    topPadding: Dp = MaterialTheme.dimens.spaceSm,
) {
    // `OutlinedTextField` doesn't reliably scroll itself into view on focus
    // alone (verified on-device) — this explicitly re-requests it once the
    // IME has actually finished opening. Gating on isImeVisible rather than
    // just isFocused matters: called the instant focus lands, bringIntoView()
    // targets the pre-keyboard viewport size and can land in the wrong place.
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var isFocused by remember { mutableStateOf(false) }
    val imeVisible = WindowInsets.isImeVisible

    LaunchedEffect(isFocused, imeVisible) {
        if (isFocused && imeVisible) {
            bringIntoViewRequester.bringIntoView()
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = topPadding)
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusEvent { isFocused = it.isFocused },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@Composable
private fun AuthFormMessages(state: LoginFormState, isSignUp: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        state.fieldError?.let { error ->
            AuthMessage(text = stringResource(error.messageRes), color = MaterialTheme.colorScheme.error)
        }
        if (state.hasError) {
            AuthMessage(
                text = stringResource(if (isSignUp) R.string.chat_signup_error else R.string.chat_login_error),
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun AuthMessage(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier.padding(top = MaterialTheme.dimens.spaceSm),
    )
}

@Composable
private fun AuthSubmitButton(
    state: LoginFormState,
    isSignUp: Boolean,
    enabled: Boolean,
    onEvent: (LoginEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = { onEvent(LoginEvent.Submit) },
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = modifier
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
            Text(stringResource(if (isSignUp) R.string.chat_signup_submit else R.string.chat_login_submit))
        }
    }
}

private val LoginFieldError.messageRes: Int
    get() = when (this) {
        LoginFieldError.MISSING_FIELDS -> R.string.chat_auth_error_missing_fields
        LoginFieldError.INVALID_EMAIL -> R.string.chat_auth_error_invalid_email
        LoginFieldError.WEAK_PASSWORD -> R.string.chat_auth_error_weak_password
        LoginFieldError.PASSWORD_MISMATCH -> R.string.chat_auth_error_password_mismatch
        LoginFieldError.INVALID_USERNAME -> R.string.chat_auth_error_invalid_username
    }

/**
 * Sign-up only. The picked image can't be uploaded yet — there's no account
 * (and no [com.example.mozic.core.network.AuthSession] access token) to
 * upload it to until `signUp()` resolves — so this only hands the raw bytes
 * back through [LoginEvent.AvatarPicked] for the ViewModel to hold onto and
 * upload once the account exists. See [ConversationListViewModel.pendingAvatar].
 */
@Composable
private fun SignUpAvatarPicker(
    avatarPreviewUri: String?,
    onPicked: (LoginEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        if (bytes != null) onPicked(LoginEvent.AvatarPicked(uri.toString(), bytes, mimeType))
    }

    Box(modifier = modifier) {
        Avatar(
            model = avatarPreviewUri,
            contentDescription = stringResource(R.string.chat_signup_avatar_cd),
            modifier = Modifier
                .size(MaterialTheme.dimens.avatarSize)
                .clip(CircleShape)
                .clickable {
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(AVATAR_BADGE_SIZE_DP.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .clickable {
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(MaterialTheme.dimens.spaceMd),
            )
        }
        if (avatarPreviewUri != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(AVATAR_BADGE_SIZE_DP.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .clickable { onPicked(LoginEvent.AvatarCleared) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.chat_signup_remove_avatar_cd),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(MaterialTheme.dimens.spaceMd),
                )
            }
        }
    }
}
