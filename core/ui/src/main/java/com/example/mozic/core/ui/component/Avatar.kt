package com.example.mozic.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.mozic.core.designsystem.theme.mozicColors

private const val AVATAR_FALLBACK_ICON_FRACTION = 1f / 3f

/**
 * Circular person avatar — a real image when [model] is non-null, otherwise
 * a gradient-filled silhouette (the look popular chat/social apps default
 * to) instead of [CoverImage]'s generic artwork-stripe fallback, which reads
 * as "still loading" rather than "this person has no photo set."
 *
 * [modifier] should carry sizing/clipping (e.g. `Modifier.size(x).clip(CircleShape)`)
 * — both branches render into it directly, same contract as [CoverImage].
 */
@Composable
fun Avatar(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    if (model != null) {
        CoverImage(
            model = model,
            contentDescription = contentDescription,
            modifier = modifier,
        )
    } else {
        Box(
            modifier = modifier.background(brush = MaterialTheme.mozicColors.accentGradient),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.fillMaxSize(AVATAR_FALLBACK_ICON_FRACTION),
            )
        }
    }
}
