package com.gabbasov.meterscan.meters.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gabbasov.meterscan.NAVIGATION_DURATION
import com.gabbasov.meterscan.features.MetersFeatureApi
import com.gabbasov.meterscan.meters.presentation.details.MeterDetailScreenRoute
import com.gabbasov.meterscan.meters.presentation.details.rememberMeterDetailCoordinator
import com.gabbasov.meterscan.meters.presentation.list.MetersListScreenRoute
import com.gabbasov.meterscan.meters.presentation.list.rememberMetersListCoordinator

object MeterRoutes {
    const val METERS_LIST = "meters_list"
    const val METER_DETAILS = "meter_details"
    const val ADD_METER = "add_meter"
    const val EDIT_METER = "edit_meter"
    const val ADD_READING = "add_reading"
}

class MetersNavigation : MetersFeatureApi {
    override fun meterListRoute() = MeterRoutes.METERS_LIST

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier,
    ) {
        navGraphBuilder.composable(
            route = meterListRoute(),
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
            MetersListScreenRoute(
                rememberMetersListCoordinator(navController = navController)
            )
        }

        navGraphBuilder.composable(
            route = "${MeterRoutes.METER_DETAILS}/{meterId}",
            arguments = listOf(
                navArgument("meterId") { type = NavType.StringType }
            ),
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
        ) { backStackEntry ->
            val meterId = backStackEntry.arguments?.getString("meterId") ?: ""
            MeterDetailScreenRoute(
                coordinator = rememberMeterDetailCoordinator(navController = navController),
                meterId = meterId
            )
        }

        // Здесь должны быть определены остальные маршруты:
        // ADD_METER, EDIT_METER, ADD_READING
        // Для краткости они опущены
    }
}