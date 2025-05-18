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
    }

    fun onNavigateBack() {
        viewModel.execute(MeterDetailAction.NavigateBack)
        navController.popBackStack()
    }

    fun onSaveReading(reading: String) {
        viewModel.execute(MeterDetailAction.SaveReading(reading))
    }

    fun onConfirmLowerValue() {
        viewModel.execute(MeterDetailAction.ConfirmLowerValue)
    }

    fun onDismissReadingDialog() {
        viewModel.execute(MeterDetailAction.DismissReadingDialog)
    }

    fun onDismissLowerValueWarning() {
        viewModel.execute(MeterDetailAction.DismissLowerValueWarning)
    }

    fun onNavigateToScan(meterId: String) {
        navController.navigate("${NavigationRoute.METER_SCAN.route}/$meterId")
        viewModel.execute(MeterDetailAction.NavigationHandled)
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