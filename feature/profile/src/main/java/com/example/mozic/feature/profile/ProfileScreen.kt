package com.example.mozic.feature.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mozic.core.designsystem.R
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.designsystem.theme.mozicColors
import com.example.mozic.core.ui.component.CoverImage

private const val AVATAR_BADGE_SIZE_DP = 28

@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val purchaseSuccessMessage = stringResource(R.string.profile_purchase_success)
    val avatarUpdateFailedMessage = stringResource(R.string.profile_avatar_update_failed)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ProfileEffect.PurchaseCompleted -> snackbarHostState.showSnackbar(purchaseSuccessMessage)
                ProfileEffect.AvatarUpdateFailed -> snackbarHostState.showSnackbar(avatarUpdateFailedMessage)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when (val state = uiState) {
            ProfileUiState.Loading -> Unit
            is ProfileUiState.Content -> ProfileContent(
                state = state,
                onEvent = viewModel::onEvent,
                onNavigateToSettings = onNavigateToSettings,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(
                        horizontal = MaterialTheme.dimens.screenHorizontalPadding,
                        vertical = MaterialTheme.dimens.spaceLg,
                    ),
            )
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileUiState.Content,
    onEvent: (ProfileEvent) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        if (state.isLoggedIn) {
            // Logged in: the real profiles.avatar_url is the source of truth,
            // so this needs actual bytes to upload, not just a local content://
            // reference (which Coil could load directly, but Supabase Storage
            // can't reach into this app's process to read it).
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            if (bytes != null) onEvent(ProfileEvent.UploadAvatar(bytes, mimeType))
        } else {
            onEvent(ProfileEvent.SetLocalAvatar(uri.toString()))
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceLg),
    ) {
        AvatarPicker(
            avatarUri = state.avatarUri,
            onClick = {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onRemoveClick = { onEvent(ProfileEvent.RemoveAvatar) },
        )

        if (state.displayName != null) {
            Text(text = state.displayName, style = MaterialTheme.typography.titleLarge)
        }

        PlanCard(
            isPremium = state.isPremium,
            isPurchasing = state.isPurchasing,
            onUpgradeClick = { onEvent(ProfileEvent.PurchasePremium) },
            modifier = Modifier.fillMaxWidth(),
        )

        SettingsEntryRow(onClick = onNavigateToSettings, modifier = Modifier.fillMaxWidth())
    }
}

/**
 * `PickVisualMedia`'s Uri is only guaranteed readable for this process's
 * lifetime (Android's Photo Picker doesn't support persistable grants) — the
 * avatar can go blank after a device restart until the user re-picks. A known
 * platform limitation, not a bug here.
 */
@Composable
private fun AvatarPicker(
    avatarUri: String?,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(MaterialTheme.dimens.avatarSize)
                .clip(CircleShape)
                .clickable(onClick = onClick),
        ) {
            if (avatarUri != null) {
                CoverImage(
                    model = avatarUri,
                    contentDescription = stringResource(R.string.profile_avatar_cd),
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = MaterialTheme.mozicColors.accentGradient),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = stringResource(R.string.profile_avatar_cd),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(MaterialTheme.dimens.spaceXl),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(AVATAR_BADGE_SIZE_DP.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(MaterialTheme.dimens.spaceMd),
            )
        }
        // Only offered once there's actually something to remove — nothing
        // to clear back to but the same default placeholder icon otherwise.
        if (avatarUri != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(AVATAR_BADGE_SIZE_DP.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .clickable(onClick = onRemoveClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.profile_remove_avatar_cd),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(MaterialTheme.dimens.spaceMd),
                )
            }
        }
    }
}

@Composable
private fun PlanCard(
    isPremium: Boolean,
    isPurchasing: Boolean,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardColors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    Card(modifier = modifier, colors = cardColors) {
        Column(
            modifier = Modifier.padding(MaterialTheme.dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceSm),
        ) {
            if (isPremium) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceXs),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(brush = MaterialTheme.mozicColors.accentGradient)
                            .padding(
                                horizontal = MaterialTheme.dimens.spaceSm,
                                vertical = MaterialTheme.dimens.spaceXxs,
                            ),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceXxs),
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
                }
            } else {
                Text(text = stringResource(R.string.premium_upsell_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.premium_upsell_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = onUpgradeClick,
                    enabled = !isPurchasing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        // Disabled here means "processing", not "inactive" — the
                        // spinner/label should stay fully legible, unlike Play
                        // All's truly-inert disabled state elsewhere.
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    modifier = Modifier.background(
                        brush = MaterialTheme.mozicColors.accentGradient,
                        shape = ButtonDefaults.shape,
                    ),
                ) {
                    if (isPurchasing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(MaterialTheme.dimens.spaceMd),
                            strokeWidth = MaterialTheme.dimens.progressStrokeWidthThin,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Text(
                            text = stringResource(R.string.profile_purchase_processing),
                            modifier = Modifier.padding(start = MaterialTheme.dimens.spaceXs),
                            fontWeight = FontWeight.ExtraBold,
                        )
                    } else {
                        Text(text = stringResource(R.string.premium_upsell_cta), fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsEntryRow(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = MaterialTheme.dimens.spaceSm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceSm),
    ) {
        Icon(imageVector = Icons.Filled.Settings, contentDescription = null)
        Text(
            text = stringResource(R.string.profile_settings_entry),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
    }
}
