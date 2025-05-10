package com.gabbasov.meterscan.scan.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.gabbasov.meterscan.NAVIGATION_DURATION
import com.gabbasov.meterscan.NavigationRoute
import com.gabbasov.meterscan.features.MeterScanFeatureApi
import com.gabbasov.meterscan.scan.presentation.MeterScanScreenRoute
import com.gabbasov.meterscan.scan.presentation.rememberMeterScanCoordinator

class MeterScanNavigation : MeterScanFeatureApi {
    override fun meterScanRoute() = NavigationRoute.METER_SCAN.route

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(
            route = meterScanRoute(),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(NAVIGATION_DURATION)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(NAVIGATION_DURATION)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(NAVIGATION_DURATION)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(NAVIGATION_DURATION)
                )
            }
        ) {
            MeterScanScreenRoute(
                coordinator = rememberMeterScanCoordinator(navController = navController)
            )
        }
    }
}