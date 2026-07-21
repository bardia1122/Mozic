package com.example.mozic.feature.social

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mozic.core.designsystem.R
import com.example.mozic.core.domain.model.User
import com.example.mozic.core.ui.component.MediaListRowSkeleton
import com.example.mozic.core.ui.component.PlaceholderScreen

private const val SKELETON_ROW_COUNT = 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowingListScreen(
    onBackClick: () -> Unit,
    onUserClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FollowingListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val actionFailedMessage = stringResource(R.string.social_action_failed)
    val loginRequiredMessage = stringResource(R.string.social_login_required)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
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
                title = { Text(stringResource(R.string.social_following_title)) },
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
            FollowingListUiState.Loading -> LazyColumn(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                items(SKELETON_ROW_COUNT) { MediaListRowSkeleton(imageShape = CircleShape) }
            }

            is FollowingListUiState.Content -> if (state.users.isEmpty()) {
                PlaceholderScreen(
                    title = stringResource(R.string.social_following_empty_title),
                    subtitle = stringResource(R.string.social_following_empty_subtitle),
                    modifier = Modifier.padding(innerPadding).fillMaxSize(),
                )
            } else {
                LazyColumn(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                    items(state.users, key = User::id) { user ->
                        UserRow(
                            user = user,
                            onClick = { onUserClick(user.id) },
                            onFollowToggle = { viewModel.onUnfollowClick(user.id) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}
