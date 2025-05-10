package com.gabbasov.meterscan.auth.presentation.signin.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.gabbasov.meterscan.NAVIGATION_DURATION
import com.gabbasov.meterscan.NavigationRoute
import com.gabbasov.meterscan.auth.presentation.signin.SignInScreenRoute
import com.gabbasov.meterscan.auth.presentation.signin.rememberSignInCoordinator
import com.gabbasov.meterscan.features.SignInFeatureApi

class SignInNavigation : SignInFeatureApi {
    override fun signInRoute() = NavigationRoute.SIGN_IN.route

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier,
    ) {
        navGraphBuilder.composable(
            route = signInRoute(),
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
            SignInScreenRoute(
                rememberSignInCoordinator(navController = navController)
            )
        }
    }
}