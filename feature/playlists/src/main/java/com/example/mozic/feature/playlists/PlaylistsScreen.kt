package com.example.mozic.feature.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mozic.core.designsystem.R as DesignSystemR
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.designsystem.theme.mozicColors
import com.example.mozic.core.domain.model.Playlist
import com.example.mozic.core.ui.component.EmptyState
import com.example.mozic.core.ui.component.PlaylistCard
import com.example.mozic.core.ui.component.PlaylistCardSkeleton
import kotlinx.coroutines.launch

// Matches SampleData's current 2-per-category count. A real backend's counts
// will vary, but for the fakes this avoids the skeleton→content swap shrinking
// the grid (12 skeleton cards → 6 real ones) and forcing LazyVerticalGrid's
// built-in "don't leave empty space below the last item" re-anchoring — the
// same underlying mechanism as the nav-transition scroll glitch fixed in
// MozicApp.kt, just triggered by the data swap instead of navigation.
private const val SKELETON_ITEMS_PER_SECTION = 2

/** Clears the floating "Create playlist" FAB — see its use at the grid's `contentPadding`. */
private val FAB_BOTTOM_CLEARANCE = 96.dp

@Composable
fun PlaylistsScreen(
    onNavigateToDetail: (Playlist) -> Unit,
    onLoginRequiredForCreate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val createPlaylistState by viewModel.createPlaylistState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()
    val playlistCreatedMessage = stringResource(DesignSystemR.string.playlists_create_success)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PlaylistsEffect.NavigateToDetail -> onNavigateToDetail(effect.playlist)
                PlaylistsEffect.LoginRequiredForCreate -> onLoginRequiredForCreate()
                PlaylistsEffect.PlaylistCreated ->
                    coroutineScope.launch { snackbarHostState.showSnackbar(playlistCreatedMessage) }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            CreatePlaylistFab(onClick = { viewModel.onEvent(PlaylistsEvent.CreatePlaylistClick) })
        },
    ) { innerPadding ->
        PlaylistsContent(
            uiState = uiState,
            gridState = gridState,
            onPlaylistClick = { viewModel.onEvent(PlaylistsEvent.PlaylistClick(it)) },
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        )
    }

    val visibleCreateState = createPlaylistState
    if (visibleCreateState is CreatePlaylistUiState.Visible) {
        CreatePlaylistDialog(
            state = visibleCreateState,
            onTitleChange = { viewModel.onEvent(PlaylistsEvent.CreatePlaylistTitleChange(it)) },
            onConfirm = { viewModel.onEvent(PlaylistsEvent.CreatePlaylistConfirm) },
            onDismiss = { viewModel.onEvent(PlaylistsEvent.CreatePlaylistDismiss) },
        )
    }
}

@Composable
private fun CreatePlaylistFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = modifier.background(
            brush = MaterialTheme.mozicColors.accentGradient,
            shape = ButtonDefaults.shape,
        ),
    ) {
        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
        Spacer(Modifier.width(MaterialTheme.dimens.spaceXs))
        Text(
            text = stringResource(DesignSystemR.string.playlists_create),
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun CreatePlaylistDialog(
    state: CreatePlaylistUiState.Visible,
    onTitleChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val canConfirm = state.title.isNotBlank() && !state.isSubmitting

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = { if (!state.isSubmitting) onDismiss() },
        title = { Text(stringResource(DesignSystemR.string.playlists_create)) },
        text = {
            Column {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = onTitleChange,
                    label = { Text(stringResource(DesignSystemR.string.playlists_create_name_label)) },
                    singleLine = true,
                    enabled = !state.isSubmitting,
                    isError = state.showError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (canConfirm) onConfirm()
                        },
                    ),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                )
                if (state.showError) {
                    Text(
                        text = stringResource(DesignSystemR.string.playlists_create_error),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = MaterialTheme.dimens.spaceXs),
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = canConfirm) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(MaterialTheme.dimens.spaceMd),
                        strokeWidth = MaterialTheme.dimens.spaceXxs / 2,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(stringResource(DesignSystemR.string.playlists_create_confirm))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !state.isSubmitting) {
                Text(stringResource(DesignSystemR.string.playlists_create_cancel))
            }
        },
    )
}

@Composable
private fun PlaylistsContent(
    uiState: PlaylistsUiState,
    gridState: LazyGridState,
    onPlaylistClick: (Playlist) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState is PlaylistsUiState.Error) {
        EmptyState(
            icon = Icons.Filled.WifiOff,
            title = stringResource(DesignSystemR.string.state_error),
            subtitle = stringResource(DesignSystemR.string.state_error_subtitle),
            modifier = modifier,
        )
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = modifier,
        contentPadding = PaddingValues(
            start = MaterialTheme.dimens.screenHorizontalPadding,
            end = MaterialTheme.dimens.screenHorizontalPadding,
            top = MaterialTheme.dimens.spaceMd,
            // A floating FAB isn't accounted for by Scaffold's own innerPadding —
            // it genuinely floats over content — so the last row needs enough
            // clearance itself: FAB height (~56dp) + Scaffold's own FAB margin
            // (~16dp) + a little breathing room above it.
            bottom = FAB_BOTTOM_CLEARANCE,
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceMd),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceMd),
    ) {
        when (uiState) {
            PlaylistsUiState.Loading -> {
                skeletonSection(DesignSystemR.string.playlists_section_world)
                skeletonSection(DesignSystemR.string.playlists_section_local)
                skeletonSection(DesignSystemR.string.home_quick_action_playlists)
            }

            is PlaylistsUiState.Content -> {
                playlistSection(DesignSystemR.string.playlists_section_world, uiState.world, onPlaylistClick)
                playlistSection(DesignSystemR.string.playlists_section_local, uiState.local, onPlaylistClick)
                playlistSection(DesignSystemR.string.home_quick_action_playlists, uiState.mine, onPlaylistClick)
            }

            PlaylistsUiState.Error -> Unit
        }
    }
}

private fun LazyGridScope.playlistSection(
    titleRes: Int,
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit,
) {
    if (playlists.isEmpty()) return
    item(span = { GridItemSpan(maxLineSpan) }) {
        SectionHeader(titleRes)
    }
    items(playlists, key = { it.id }) { playlist ->
        Box(
            modifier = Modifier.fillMaxWidth().animateItem(),
            contentAlignment = Alignment.Center,
        ) {
            PlaylistCard(playlist = playlist, onClick = { onPlaylistClick(playlist) })
        }
    }
}

private fun LazyGridScope.skeletonSection(titleRes: Int) {
    item(span = { GridItemSpan(maxLineSpan) }) {
        SectionHeader(titleRes)
    }
    items(SKELETON_ITEMS_PER_SECTION) {
        Box(
            modifier = Modifier.fillMaxWidth().animateItem(),
            contentAlignment = Alignment.Center,
        ) {
            PlaylistCardSkeleton()
        }
    }
}

@Composable
private fun SectionHeader(titleRes: Int) {
    Text(text = stringResource(titleRes), style = MaterialTheme.typography.titleMedium)
}
