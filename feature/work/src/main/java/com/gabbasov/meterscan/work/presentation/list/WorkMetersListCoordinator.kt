package com.gabbasov.meterscan.work.presentation.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.gabbasov.meterscan.NavigationRoute
import androidx.navigation.NavHostController
import org.koin.androidx.compose.koinViewModel

internal class WorkMetersListCoordinator(
    private val viewModel: WorkMetersListViewModel,
    private val navController: NavHostController,
) {
    val state = viewModel.uiState

    fun onMeterSelected(meterId: String) {
        viewModel.execute(WorkMetersListAction.MeterSelected(meterId))
        navController.navigate("${NavigationRoute.METER_DETAILS}/$meterId")
    }

    fun onRefresh() {
        viewModel.execute(WorkMetersListAction.LoadMeters)
    }

    fun onSearchQueryChanged(query: String) {
        viewModel.execute(WorkMetersListAction.SearchMeters(query))
    }
}

@Composable
internal fun rememberWorkMetersListCoordinator(
    viewModel: WorkMetersListViewModel = koinViewModel(),
    navController: NavHostController,
): WorkMetersListCoordinator {
    return remember(viewModel, navController) {
        WorkMetersListCoordinator(
            viewModel = viewModel,
            navController = navController,
        )
    }
}