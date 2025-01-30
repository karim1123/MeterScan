package com.gabbasov.meterscan.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    NavHost(
        navController = navController,
        startDestination = viewModel.signUpApi.signUpRoute(),
    ) {
        register(
            modifier = modifier,
            navController = navController,
            featureApi = viewModel.signUpApi,
        )
    }
}
