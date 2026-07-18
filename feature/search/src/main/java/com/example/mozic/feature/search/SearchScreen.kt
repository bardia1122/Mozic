package com.example.mozic.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.mozic.core.designsystem.R as DesignSystemR
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.feature.search.component.FilterChipsRow
import com.example.mozic.feature.search.component.SearchField
import com.example.mozic.feature.search.component.SearchHistoryList
import com.example.mozic.feature.search.component.SearchResultsList

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagingItems = viewModel.results.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    val comingSoonMessage = stringResource(DesignSystemR.string.placeholder_coming_soon)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                SearchEffect.ShowComingSoon -> snackbarHostState.showSnackbar(comingSoonMessage)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(top = MaterialTheme.dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spaceMd),
        ) {
            SearchField(
                query = uiState.query,
                onQueryChange = { viewModel.onEvent(SearchEvent.QueryChanged(it)) },
                onSearch = { viewModel.onEvent(SearchEvent.Submit) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.dimens.screenHorizontalPadding),
            )

            if (uiState.query.isBlank()) {
                SearchHistoryList(
                    history = uiState.history,
                    onItemClick = { viewModel.onEvent(SearchEvent.HistoryItemClick(it)) },
                    onItemRemove = { viewModel.onEvent(SearchEvent.HistoryItemRemove(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            } else {
                FilterChipsRow(
                    selected = uiState.filter,
                    onFilterClick = { viewModel.onEvent(SearchEvent.FilterChanged(it)) },
                )
                SearchResultsList(
                    pagingItems = pagingItems,
                    onResultClick = viewModel::onResultClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }
    }
}
