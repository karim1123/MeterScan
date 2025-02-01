package com.gabbasov.meterscan

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

const val NAVIGATION_DURATION = 500

fun NavGraphBuilder.register(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    featureApi: FeatureApi,
) {
    featureApi.registerGraph(
        navGraphBuilder = this,
        navController = navController,
        modifier = modifier,
    )
}
