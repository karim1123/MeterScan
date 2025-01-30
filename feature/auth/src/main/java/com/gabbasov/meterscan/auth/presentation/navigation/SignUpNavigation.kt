package com.gabbasov.meterscan.auth.presentation.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.gabbasov.meterscan.NavigationRoute
import com.gabbasov.meterscan.features.SignUpFeatureApi
import com.gabbasov.meterscan.auth.presentation.SignUpScreenRoute

class SignUpNavigation : SignUpFeatureApi {
    override fun signUpRoute() = NavigationRoute.SIGN_UP.route

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier,
    ) {
        navGraphBuilder.composable(signUpRoute()) {
            SignUpScreenRoute(
                navController,
            )
        }
    }
}
