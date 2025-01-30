package com.gabbasov.meterscan.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.gabbasov.meterscan.register
import org.koin.androidx.compose.koinViewModel

@Composable
fun MeterScanNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: MainActivityViewModel = koinViewModel(),
) {
    val isAuthorized by viewModel.isAuthorized.collectAsStateWithLifecycle(false)

    NavHost(
        navController = navController,
        startDestination = viewModel.signInApi.signInRoute(),
    ) {
        register(
            modifier = modifier,
            navController = navController,
            featureApi = viewModel.signInApi,
        )
        register(
            modifier = modifier,
            navController = navController,
            featureApi = viewModel.signUpApi,
        )
    }
}
