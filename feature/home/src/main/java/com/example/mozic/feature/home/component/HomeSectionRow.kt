package com.example.mozic.feature.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.mozic.core.designsystem.theme.dimens

/** Title + horizontally scrolling row, the shared shape for every Home section (and its skeleton). */
@Composable
fun HomeSectionRow(
    title: String,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = MaterialTheme.dimens.spaceMd),
        )
        LazyRow(
            modifier = Modifier.padding(top = MaterialTheme.dimens.spaceXs),
            contentPadding = PaddingValues(horizontal = MaterialTheme.dimens.spaceMd),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceMd),
            content = content,
        )
    }
}
