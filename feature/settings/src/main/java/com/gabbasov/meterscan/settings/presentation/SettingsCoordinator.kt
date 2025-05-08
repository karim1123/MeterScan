package com.gabbasov.meterscan.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.gabbasov.meterscan.model.navigator.NavigatorType
import org.koin.androidx.compose.koinViewModel

class SettingsCoordinator(
    private val viewModel: SettingsViewModel,
    private val navController: NavHostController
) {
    val state = viewModel.uiState

    fun onCameraModeChanged(enabled: Boolean) {
        viewModel.execute(SettingsAction.SetCameraMode(enabled))
    }

    fun onNavigatorTypeChanged(type: NavigatorType) {
        viewModel.execute(SettingsAction.SetNavigatorType(type))
    }

    fun onSignOutClicked() {
        viewModel.execute(SettingsAction.SignOut)
        NavigationHolder.rootNavController?.navigate(viewModel.signInApi.signInRoute()) {
            popUpTo(0) { inclusive = true }
        }
    }
}

@Composable
fun rememberSettingsCoordinator(
    viewModel: SettingsViewModel = koinViewModel(),
    navController: NavHostController
): SettingsCoordinator {
    return remember(viewModel, navController) {
        SettingsCoordinator(
            viewModel = viewModel,
            navController = navController
        )
    }
}

object NavigationHolder {
    var rootNavController: NavHostController? = null
}