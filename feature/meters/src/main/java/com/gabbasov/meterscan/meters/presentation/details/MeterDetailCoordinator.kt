package com.gabbasov.meterscan.meters.presentation.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.gabbasov.meterscan.meters.presentation.navigation.MeterRoutes
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
            navController.navigate("${MeterRoutes.ADD_READING}/$meterId")
        }
    }

    fun onEditMeter() {
        viewModel.execute(MeterDetailAction.EditMeter)
        state.value.meter?.id?.let { meterId ->
            navController.navigate("${MeterRoutes.EDIT_METER}/$meterId")
        }
    }

    fun onDeleteMeter() {
        viewModel.execute(MeterDetailAction.DeleteMeter)
        // После успешного удаления вернуться к списку
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