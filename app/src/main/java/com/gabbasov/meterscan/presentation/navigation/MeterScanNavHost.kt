package com.gabbasov.meterscan.presentation.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.gabbasov.meterscan.presentation.MainActivityViewModel
import com.gabbasov.meterscan.register
import com.gabbasov.meterscan.ui.NavigationHolder
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MeterScanNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberAnimatedNavController(),
    viewModel: MainActivityViewModel = koinViewModel(),
) {
    NavigationHolder.rootNavController = navController
    val isAuthorized by viewModel.isAuthorized.collectAsStateWithLifecycle(false)

    NavHost(
        navController = navController,
        startDestination = if (isAuthorized) viewModel.mainScreenApi.mainScreenRoute() else viewModel.signInApi.signInRoute(),
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
        register(
            modifier = modifier,
            navController = navController,
            featureApi = viewModel.metersFeatureApi,
        )
        register(
            modifier = modifier,
            navController = navController,
            featureApi = viewModel.mainScreenApi,
        )
    }
}
