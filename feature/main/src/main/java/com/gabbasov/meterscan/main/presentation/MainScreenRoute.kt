package com.gabbasov.meterscan.main.presentation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gabbasov.meterscan.designsystem.MeterScanTheme
import com.gabbasov.meterscan.main.presentation.tabs.MainTab
import com.gabbasov.meterscan.register
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreenRoute(
    modifier: Modifier = Modifier
) {
    MeterScanTheme {
        MainScreen(
            modifier = modifier
        )
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
) {
    val bottomNavController = rememberNavController()
    var selectedTab by remember { mutableStateOf(MainTab.METERS) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            MainBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                    when (tab) {
                        MainTab.METERS -> bottomNavController.navigate(MainTab.METERS.route) {
                            launchSingleTop = true
                            restoreState = true
                        }

                        MainTab.CAMERA -> bottomNavController.navigate(MainTab.CAMERA.route) {
                            launchSingleTop = true
                            restoreState = true
                        }

                        MainTab.WORK -> bottomNavController.navigate(MainTab.WORK.name) {
                            launchSingleTop = true
                            restoreState = true
                        }

                        MainTab.SETTINGS -> bottomNavController.navigate(MainTab.SETTINGS.name) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            MainTabsNavHost(navController = bottomNavController)
        }
    }
}

@Composable
fun MainBottomNavigation(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = tab.icon),
                        contentDescription = stringResource(tab.titleResId)
                    )
                },
                label = { Text(stringResource(tab.titleResId)) },
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) }
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainTabsNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberAnimatedNavController(),
    viewModel: MainViewModel = koinViewModel(),
) {
    NavHost(
        navController = navController,
        startDestination = viewModel.metersFeatureApi.meterListRoute()
    ) {
        register(
            modifier = modifier,
            navController = navController,
            featureApi = viewModel.meterScanFeatureApi,
        )
        composable(MainTab.WORK.name) {
            WorkScreen(navController = navController)
        }
        register(
            modifier = modifier,
            navController = navController,
            featureApi = viewModel.metersFeatureApi,
        )
        register(
            modifier = modifier,
            navController = navController,
            featureApi = viewModel.settingsFeatureApi,
        )
    }
}


///////////////////////////////////////////////////////////

@Composable
fun CameraScreen(navController: NavHostController) {
    // Содержимое экрана камеры
    Box {
        Text(text = "Экран камеры")
    }
}

@Composable
fun WorkScreen(navController: NavHostController) {
    // Содержимое экрана работы
    Box {
        Text(text = "Экран работы")
    }
}

@Composable
fun MetersScreen(navController: NavHostController) {
    // Здесь можно вызвать экран из MetersFeatureApi или перенаправить на него
    Box {
        Text(text = "Экран счетчиков")
    }
}

@Composable
fun SettingsScreen(navController: NavHostController) {
    // Содержимое экрана настроек
    Box {
        Text(text = "Экран настроек")
    }
}