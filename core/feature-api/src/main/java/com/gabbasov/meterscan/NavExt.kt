package com.gabbasov.meterscan

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

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
