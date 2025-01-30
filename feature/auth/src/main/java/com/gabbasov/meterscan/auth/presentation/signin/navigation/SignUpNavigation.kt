package com.gabbasov.meterscan.auth.presentation.signin.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
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
        navGraphBuilder.composable(signInRoute()) {
            SignInScreenRoute(
                rememberSignInCoordinator(navController = navController),
            )
        }
    }
}
