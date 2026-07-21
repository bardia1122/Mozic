package com.example.mozic.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.example.mozic.core.designsystem.theme.dimens

/**
 * Shared illustrated empty/error state (I2 — `doc/PLAN_PERSON_B.md`'s
 * "empty states: illustrated, not blank" requirement). Replaces the old
 * text-only `PlaceholderScreen` plus every feature module's hand-rolled
 * equivalent (`EmptyLibraryMessage`, `DownloadsEmptyMessage`, an inline
 * `Column` in `UserProfileScreen`) so every empty/error surface in the app
 * shares one look. Wrap-content by design — callers own the sizing: pass
 * `Modifier.fillMaxSize()` for a whole-screen slot (mirrors
 * `PlaceholderScreen`'s old behavior) or `Modifier.fillMaxWidth()` for a
 * `LazyColumn`/`LazyVerticalGrid` `item {}` (unbounded height axis).
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceXs),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(MaterialTheme.dimens.emptyStateIconSize),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            if (actionLabel != null && onAction != null) {
                Button(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}
