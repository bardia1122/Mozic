package com.example.mozic.feature.social

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.mozic.core.designsystem.R
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.domain.model.User
import com.example.mozic.core.ui.component.EmptyState
import com.example.mozic.core.ui.component.FollowIconButton
import com.example.mozic.core.ui.component.MediaListRow
import com.example.mozic.core.ui.component.MediaListRowSkeleton

private const val SKELETON_ROW_COUNT = 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSearchScreen(
    onBackClick: () -> Unit,
    onUserClick: (String) -> Unit,
    onFollowingClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UserSearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagingItems = viewModel.results.collectAsLazyPagingItems()
    val followedIds by viewModel.followedIds.collectAsStateWithLifecycle()
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
                title = { Text(stringResource(R.string.social_search_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onFollowingClick) {
                        Icon(
                            imageVector = Icons.Filled.People,
                            contentDescription = stringResource(R.string.cd_view_following),
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
                .padding(top = MaterialTheme.dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceMd),
        ) {
            UserSearchField(
                query = uiState.query,
                onQueryChange = { viewModel.onEvent(UserSearchEvent.QueryChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.dimens.screenHorizontalPadding),
            )

            if (uiState.query.isBlank()) {
                EmptyState(
                    icon = Icons.Filled.PersonSearch,
                    title = stringResource(R.string.social_search_title),
                    subtitle = stringResource(R.string.social_search_placeholder),
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
            } else {
                UserResultsList(
                    pagingItems = pagingItems,
                    followedIds = followedIds,
                    onUserClick = { onUserClick(it.id) },
                    onFollowToggle = { user ->
                        viewModel.onEvent(UserSearchEvent.FollowToggle(user.id, user.id in followedIds))
                    },
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
            }
        }
    }
}

@Composable
private fun UserSearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        placeholder = {
            Text(
                text = stringResource(R.string.social_search_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = stringResource(R.string.cd_clear_search),
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {}),
    )
}

@Composable
private fun UserResultsList(
    pagingItems: LazyPagingItems<User>,
    followedIds: Set<String>,
    onUserClick: (User) -> Unit,
    onFollowToggle: (User) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isInitialLoad = pagingItems.loadState.refresh is LoadState.Loading
    val isEmpty = pagingItems.loadState.refresh is LoadState.NotLoading && pagingItems.itemCount == 0

    when {
        isInitialLoad -> LazyColumn(modifier = modifier) {
            items(SKELETON_ROW_COUNT) { MediaListRowSkeleton(imageShape = CircleShape) }
        }

        isEmpty -> EmptyState(
            icon = Icons.Filled.SearchOff,
            title = stringResource(R.string.social_no_users_found),
            subtitle = stringResource(R.string.state_empty),
            modifier = modifier,
        )

        else -> LazyColumn(modifier = modifier) {
            items(pagingItems.itemCount) { index ->
                pagingItems[index]?.let { user ->
                    UserRow(
                        user = user,
                        isFollowed = user.id in followedIds,
                        onClick = { onUserClick(user) },
                        onFollowToggle = { onFollowToggle(user) },
                    )
                }
            }
            if (pagingItems.loadState.append is LoadState.Loading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(MaterialTheme.dimens.spaceMd),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
internal fun UserRow(
    user: User,
    isFollowed: Boolean,
    onClick: () -> Unit,
    onFollowToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MediaListRow(
        imageUrl = user.avatarUrl,
        title = user.displayName,
        subtitle = "@${user.username}",
        onClick = onClick,
        imageShape = CircleShape,
        modifier = modifier,
        trailing = { FollowIconButton(isFollowed = isFollowed, onClick = onFollowToggle) },
    )
}
