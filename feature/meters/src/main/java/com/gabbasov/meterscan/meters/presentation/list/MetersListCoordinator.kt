package com.gabbasov.meterscan.meters.presentation.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.gabbasov.meterscan.meters.presentation.navigation.MeterRoutes
import org.koin.androidx.compose.koinViewModel

internal class MetersListCoordinator(
    private val viewModel: MetersListViewModel,
    private val navController: NavHostController,
) {
    val state = viewModel.uiState

    fun onMeterSelected(meterId: String) {
        viewModel.execute(MetersListAction.MeterSelected(meterId))
        navController.navigate("${MeterRoutes.METER_DETAILS}/$meterId")
    }

    fun onAddNewMeter() {
        viewModel.execute(MetersListAction.AddNewMeter)
        navController.navigate(MeterRoutes.ADD_METER)
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