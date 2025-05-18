package com.gabbasov.meterscan.work.presentation.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabbasov.meterscan.ui.components.meters.SearchTextField
import com.gabbasov.meterscan.work.presentation.list.pager.WorkScreenViewPager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WorkScreenRoute(
    coordinator: WorkCoordinator
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

    LaunchedEffect(state.navigateToScan) {
        state.navigateToScan?.let { meterId ->
            coordinator.onNavigationHandled()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Работа со счетчиками") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchTextField(
                value = state.searchQuery,
                onValueChange = coordinator::onSearchQueryChanged
            )

            WorkScreenViewPager(
                metersState = state,
                onMeterClick = coordinator::onMeterSelected,
                onTakeReading = coordinator::onTakeReading,
                modifier = Modifier.weight(1f)
            )
        }
    }
}