package com.example.mozic.feature.search.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.mozic.core.designsystem.R
import com.example.mozic.core.designsystem.theme.dimens

@Composable
fun SearchHistoryList(
    history: List<String>,
    onItemClick: (String) -> Unit,
    onItemRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (history.isEmpty()) return

    LazyColumn(modifier = modifier) {
        item {
            Text(
                text = stringResource(R.string.search_history_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.dimens.screenHorizontalPadding,
                    vertical = MaterialTheme.dimens.spaceXs,
                ),
            )
        }
        historyItems(history, onItemClick, onItemRemove)
    }
}

private fun LazyListScope.historyItems(
    history: List<String>,
    onItemClick: (String) -> Unit,
    onItemRemove: (String) -> Unit,
) {
    items(history, key = { it }) { query ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onItemClick(query) }
                .padding(
                    horizontal = MaterialTheme.dimens.screenHorizontalPadding,
                    vertical = MaterialTheme.dimens.spaceXs,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceMd),
        ) {
            Icon(
                imageVector = Icons.Filled.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = query,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { onItemRemove(query) }) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.cd_remove_search_history_item),
                )
            }
        }
    }
}
