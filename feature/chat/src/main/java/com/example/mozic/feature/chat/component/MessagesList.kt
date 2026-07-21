package com.example.mozic.feature.chat.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.paging.compose.LazyPagingItems
import com.example.mozic.core.designsystem.R
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.domain.model.chat.Message
import com.example.mozic.core.ui.component.EmptyState

/**
 * `reverseLayout = true`: list order is newest-first (matches
 * [com.example.mozic.core.domain.repository.ChatRepository.messages]'s
 * contract, and Room's real chat `PagingSource` per the plan), so index 0
 * renders at the bottom and inserts animate in there. A day separator is
 * drawn above a message whenever the next (chronologically older) item falls
 * on a different day — or there is no older item — by prepending it inside
 * that same item's own Column, since a `LazyColumn` item's internal content
 * always lays out top-to-bottom regardless of the list's reverseLayout.
 */
@Composable
fun MessagesList(
    pagingItems: LazyPagingItems<Message>,
    peerId: String?,
    onSongShareClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (pagingItems.itemCount == 0) {
        EmptyState(
            icon = Icons.AutoMirrored.Filled.Chat,
            title = stringResource(R.string.chat_thread_empty),
            subtitle = stringResource(R.string.chat_thread_empty_subtitle),
            modifier = modifier,
        )
        return
    }

    val listState = rememberLazyListState()

    // Index 0 is the newest message (see the reverseLayout note above), so
    // scrolling there is "scroll to bottom" — re-run whenever a message is
    // sent or received (itemCount grows), not just on first composition.
    LaunchedEffect(pagingItems.itemCount) {
        if (pagingItems.itemCount > 0) {
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        reverseLayout = true,
        contentPadding = PaddingValues(
            horizontal = MaterialTheme.dimens.screenHorizontalPadding,
            vertical = MaterialTheme.dimens.spaceSm,
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceXs),
    ) {
        items(pagingItems.itemCount, key = { index -> pagingItems.peek(index)?.id ?: index }) { index ->
            val message = pagingItems[index] ?: return@items
            val older = if (index + 1 < pagingItems.itemCount) pagingItems[index + 1] else null
            val needsSeparator = older == null || !isSameDay(message.sentAtEpochMs, older.sentAtEpochMs)

            Column {
                if (needsSeparator) {
                    DaySeparator(label = formatDaySeparator(message.sentAtEpochMs))
                }
                MessageBubble(
                    message = message,
                    isOwn = message.senderId != peerId,
                    onSongShareClick = onSongShareClick,
                )
            }
        }
    }
}
