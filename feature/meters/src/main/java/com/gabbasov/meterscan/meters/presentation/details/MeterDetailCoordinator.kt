package com.gabbasov.meterscan.meters.presentation.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.gabbasov.meterscan.NavigationRoute
import org.koin.androidx.compose.koinViewModel

internal class MeterDetailCoordinator(
    private val viewModel: MeterDetailViewModel,
    private val navController: NavHostController,
) {
    val state = viewModel.uiState

    fun loadMeter(meterId: String) {
        viewModel.execute(MeterDetailAction.LoadMeter(meterId))
    }

    fun onAddReading() {
        viewModel.execute(MeterDetailAction.AddReading)
        state.value.meter?.id?.let { meterId ->
            navController.navigate("${NavigationRoute.ADD_READING}/$meterId")
        }
    }

    fun onDeleteMeter() {
        viewModel.execute(MeterDetailAction.DeleteMeter)
        navController.popBackStack()
    }

    fun onNavigateBack() {
        viewModel.execute(MeterDetailAction.NavigateBack)
        navController.popBackStack()
    }
}

@Composable
internal fun rememberMeterDetailCoordinator(
    viewModel: MeterDetailViewModel = koinViewModel(),
    navController: NavHostController,
): MeterDetailCoordinator {
    return remember(viewModel, navController) {
        MeterDetailCoordinator(
            viewModel = viewModel,
            navController = navController,
        )
    }
}