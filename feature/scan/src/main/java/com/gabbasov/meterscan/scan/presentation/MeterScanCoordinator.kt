package com.gabbasov.meterscan.scan.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.gabbasov.meterscan.NavigationRoute
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.scan.domain.DigitBox
import org.koin.androidx.compose.koinViewModel

class MeterScanCoordinator(
    private val viewModel: MeterScanViewModel,
    private val navController: NavHostController
) {
    val state = viewModel.uiState

    fun loadMeter(meterId: String?) {
        if (!meterId.isNullOrBlank()) {
            viewModel.execute(MeterScanAction.LoadMeter(meterId))
        }
    }

    fun onDigitsDetected(digitBoxes: List<DigitBox>) {
        viewModel.execute(MeterScanAction.DetectDigits(digitBoxes))
    }

    fun onReadingUpdated(reading: String) {
        viewModel.execute(MeterScanAction.UpdateReading(reading))
    }

    fun onSaveReading() {
        val state = state.value
        viewModel.execute(MeterScanAction.SaveReading(state.meterReading))
    }

    fun onRetryScanning() {
        viewModel.execute(MeterScanAction.RetryScanning)
    }

    fun onEnterManually() {
        viewModel.execute(MeterScanAction.EnterManually)
    }

    fun onDismissBottomSheet() {
        viewModel.execute(MeterScanAction.DismissBottomSheet)
    }

    fun onDismissErrorDialog() {
        viewModel.execute(MeterScanAction.DismissErrorDialog)
    }

    fun getConfidenceThreshold() = viewModel.getСonfidenceThreshold()

    fun getHighConfidenceThreshold() = viewModel.getHighConfidenceThreshold()

    fun onToggleFlashlight() {
        viewModel.execute(MeterScanAction.ToggleFlashlight)
    }

    fun onRotateCamera() {
        viewModel.execute(MeterScanAction.RotateCamera)
    }

    fun onTogglePause() {
        viewModel.execute(MeterScanAction.TogglePause)
    }

    fun onSelectMeter(meter: Meter) {
        viewModel.execute(MeterScanAction.SelectMeter(meter))
    }

    fun onShowMeterSelection() {
        viewModel.execute(MeterScanAction.ShowMeterSelection)
    }

    fun onHideMeterSelection() {
        viewModel.execute(MeterScanAction.HideMeterSelection)
    }

    fun navigateToMetersList() {
        navController.navigate(NavigationRoute.METERS_LIST.route)
    }
}

@Composable
fun rememberMeterScanCoordinator(
    viewModel: MeterScanViewModel = koinViewModel(),
    navController: NavHostController
): MeterScanCoordinator {
    return remember(viewModel, navController) {
        MeterScanCoordinator(
            viewModel = viewModel,
            navController = navController
        )
    }
}