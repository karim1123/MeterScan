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
        NavigationHolder.rootNavController?.navigate("${NavigationRoute.METER_SCAN.route}/$meterId?goBack=true")
    }

    fun onNavigationHandled() {
        viewModel.execute(WorkAction.NavigationHandled)
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
