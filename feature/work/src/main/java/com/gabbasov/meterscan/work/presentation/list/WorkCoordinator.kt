package com.gabbasov.meterscan.work.presentation.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.gabbasov.meterscan.NavigationRoute
import androidx.navigation.NavHostController
import com.gabbasov.meterscan.ui.NavigationHolder
import org.koin.androidx.compose.koinViewModel

internal class WorkCoordinator(
    private val viewModel: WorkViewModel,
    private val navController: NavHostController,
) {
    val state = viewModel.uiState

    fun onMeterSelected(meterId: String) {
        //viewModel.execute(WorkAction.MeterSelected(meterId))
        //navController.navigate("${NavigationRoute.METER_DETAILS}/$meterId")
    }

    fun onSearchQueryChanged(query: String) {
        viewModel.execute(WorkAction.SearchMeters(query))
    }

    fun onTakeReading(meterId: String) {
        viewModel.execute(WorkAction.ShowReadingDialog(meterId))
    }

    fun onNavigationHandled() {
        viewModel.execute(WorkAction.NavigationHandled)
    }

    fun onSaveReading(reading: String) {
        viewModel.execute(WorkAction.SaveReading(reading))
    }

    fun onConfirmLowerValue() {
        viewModel.execute(WorkAction.ConfirmLowerValue)
    }

    fun onDismissReadingDialog() {
        viewModel.execute(WorkAction.DismissReadingDialog)
    }

    fun onDismissLowerValueWarning() {
        viewModel.execute(WorkAction.DismissLowerValueWarning)
    }
}

@Composable
internal fun rememberWorkMetersListCoordinator(
    viewModel: WorkViewModel = koinViewModel(),
    navController: NavHostController,
): WorkCoordinator {
    return remember(viewModel, navController) {
        WorkCoordinator(
            viewModel = viewModel,
            navController = navController,
        )
    }
}