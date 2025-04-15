package com.gabbasov.meterscan.meters.presentation.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.gabbasov.meterscan.NavigationRoute
import androidx.navigation.NavHostController
import org.koin.androidx.compose.koinViewModel

internal class MetersListCoordinator(
    private val viewModel: MetersListViewModel,
    private val navController: NavHostController,
) {
    val state = viewModel.uiState

    fun onMeterSelected(meterId: String) {
        viewModel.execute(MetersListAction.MeterSelected(meterId))
        navController.navigate("${NavigationRoute.METER_DETAILS}/$meterId")
    }

    fun onRefresh() {
        viewModel.execute(MetersListAction.LoadMeters)
    }
}

@Composable
internal fun rememberMetersListCoordinator(
    viewModel: MetersListViewModel = koinViewModel(),
    navController: NavHostController,
): MetersListCoordinator {
    return remember(viewModel, navController) {
        MetersListCoordinator(
            viewModel = viewModel,
            navController = navController,
        )
    }
}