package com.example.mozic.feature.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mozic.core.designsystem.R
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.designsystem.theme.mozicColors
import com.example.mozic.core.domain.model.AppLanguage
import com.example.mozic.core.domain.model.ThemeSetting
import com.example.mozic.core.domain.model.UserPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val loggedOutMessage = stringResource(R.string.settings_logged_out)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                SettingsEffect.LoggedOut -> snackbarHostState.showSnackbar(loggedOutMessage)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
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
            SettingsUiState.Loading -> Unit
            is SettingsUiState.Ready -> SettingsContent(
                preferences = state.preferences,
                onEvent = viewModel::onEvent,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(
                        horizontal = MaterialTheme.dimens.screenHorizontalPadding,
                        vertical = MaterialTheme.dimens.spaceMd,
                    ),
            )
        }
    }
}

@Composable
private fun SettingsContent(
    preferences: UserPreferences,
    onEvent: (SettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceLg),
    ) {
        SettingsSection(title = stringResource(R.string.settings_theme)) {
            ThemeSetting.entries.forEach { theme ->
                SettingsRadioRow(
                    label = stringResource(theme.labelRes()),
                    selected = preferences.theme == theme,
                    onClick = { onEvent(SettingsEvent.SetTheme(theme)) },
                )
            }
        }

        SettingsSection(title = stringResource(R.string.settings_language)) {
            AppLanguage.entries.forEach { language ->
                SettingsRadioRow(
                    label = stringResource(language.labelRes()),
                    selected = preferences.language == language,
                    onClick = {
                        onEvent(SettingsEvent.SetLanguage(language))
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(language.tag),
                        )
                    },
                )
            }
        }

        OutlinedButton(
            onClick = { onEvent(SettingsEvent.Logout) },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = BorderStroke(1.dp, MaterialTheme.mozicColors.destructiveBorder),
        ) {
            Text(stringResource(R.string.settings_logout))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(MaterialTheme.dimens.spaceXs))
        content()
    }
}

@Composable
private fun SettingsRadioRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = MaterialTheme.dimens.spaceXxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(MaterialTheme.dimens.spaceXs))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.Bold else null,
        )
    }
}

private fun ThemeSetting.labelRes(): Int = when (this) {
    ThemeSetting.LIGHT -> R.string.theme_light
    ThemeSetting.DARK -> R.string.theme_dark
    ThemeSetting.SYSTEM -> R.string.theme_system
}

private fun AppLanguage.labelRes(): Int = when (this) {
    AppLanguage.EN -> R.string.language_english
    AppLanguage.FA -> R.string.language_persian
}

private val AppLanguage.tag: String
    get() = when (this) {
        AppLanguage.EN -> "en"
        AppLanguage.FA -> "fa"
    }
