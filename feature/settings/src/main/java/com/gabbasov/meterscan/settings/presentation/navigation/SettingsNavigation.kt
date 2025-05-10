package com.gabbasov.meterscan.settings.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.gabbasov.meterscan.NAVIGATION_DURATION
import com.gabbasov.meterscan.NavigationRoute
import com.gabbasov.meterscan.features.SettingsFeatureApi
import com.gabbasov.meterscan.settings.presentation.SettingsScreenRoute
import com.gabbasov.meterscan.settings.presentation.rememberSettingsCoordinator

class SettingsNavigation : SettingsFeatureApi {
    override fun settingsRoute() = NavigationRoute.SETTINGS.route

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(
            route = settingsRoute(),
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
            SettingsScreenRoute(
                coordinator = rememberSettingsCoordinator(navController = navController)
            )
        }
    }
}