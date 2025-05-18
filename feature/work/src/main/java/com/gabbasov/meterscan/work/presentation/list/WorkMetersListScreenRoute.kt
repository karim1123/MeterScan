package com.gabbasov.meterscan.work.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabbasov.meterscan.ui.components.meters.MeterListItem
import com.gabbasov.meterscan.ui.components.meters.SearchTextField
import com.gabbasov.meterscan.work.R
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WorkMetersListScreenRoute(
    coordinator: WorkMetersListCoordinator
) = with(LocalContext.current) {
    val state by coordinator.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state.error) {
        state.error?.let { errorText ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(errorText.asText())
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Cчетчики") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        WorkMetersListScreen(
            modifier = Modifier.padding(paddingValues),
            state = state,
            onMeterClick = coordinator::onMeterSelected,
            onRefresh = coordinator::onRefresh,
            onSearchQueryChanged = coordinator::onSearchQueryChanged
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun WorkMetersListScreen(
    modifier: Modifier = Modifier,
    state: WorkMetersListState,
    onMeterClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onSearchQueryChanged: (String) -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isLoading,
        onRefresh = onRefresh
    )

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Поле поиска
        SearchTextField(
            value = state.searchQuery,
            onValueChange = onSearchQueryChanged
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            when {
                state.isLoading && state.meters.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                state.filteredMeters.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (state.searchQuery.isNotEmpty())
                                stringResource(R.string.meters_not_found)
                            else
                                stringResource(R.string.no_meters_available)
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.filteredMeters, key = { it.id }) { meter ->
                            MeterListItem(
                                meter = meter,
                                onClick = { onMeterClick(meter.id) }
                            )
                        }
                    }
                }
            }
            PullRefreshIndicator(
                refreshing = state.isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Preview
@Composable
private fun MetersListScreenPreview() {
    WorkMetersListScreen(
        state = WorkMetersListState(
            meters = persistentListOf(),
            filteredMeters = persistentListOf(),
            searchQuery = "",
            isLoading = false,
            error = null
        ),
        onMeterClick = {},
        onRefresh = {},
        onSearchQueryChanged = {}
    )
}