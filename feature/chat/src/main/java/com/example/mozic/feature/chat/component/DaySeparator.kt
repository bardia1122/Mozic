package com.example.mozic.feature.chat.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mozic.core.designsystem.theme.dimens

private val PillShape = RoundedCornerShape(percent = 50)
private val BorderWidth = 1.dp

/** Same pill/border-chip treatment as `FilterChipsRow`'s inactive chip. */
@Composable
fun DaySeparator(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.dimens.spaceSm),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .border(BorderStroke(BorderWidth, MaterialTheme.colorScheme.outline), PillShape)
                .padding(
                    horizontal = MaterialTheme.dimens.spaceMd,
                    vertical = MaterialTheme.dimens.spaceXxs,
                ),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
