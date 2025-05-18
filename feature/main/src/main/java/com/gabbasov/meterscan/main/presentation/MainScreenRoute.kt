package com.gabbasov.meterscan.main.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gabbasov.meterscan.NavigationRoute
import com.gabbasov.meterscan.designsystem.MeterScanTheme
import com.gabbasov.meterscan.main.presentation.tabs.MainTab
import com.gabbasov.meterscan.register
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
    viewModel: MainViewModel = koinViewModel()
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.METERS) }

    // Создаем map для NavController'ов один раз
    val navControllers = remember {
        mutableStateMapOf<MainTab, NavHostController>()
    }

    // Запоминаем созданные NavHost'ы
    val initializedTabs = remember { mutableSetOf<MainTab>() }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            MainBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            MainTab.entries.forEach { tab ->
                // Показываем только активную вкладку, но создаём все для сохранения состояния
                if (initializedTabs.contains(tab) || selectedTab == tab) {
                    initializedTabs.add(tab)

                    // Инициализируем NavController только один раз для каждой вкладки
                    val navController = navControllers.getOrPut(tab) {
                        rememberNavController()
                    }

                    AnimatedVisibility(
                        visible = selectedTab == tab,
                        enter = fadeIn() + slideInVertically { it / 4 },
                        exit = fadeOut() + slideOutVertically { it / 4 }
                    ) {
                        when (tab) {
                            MainTab.CAMERA -> CameraTabNavHost(
                                navController = navController,
                                viewModel = viewModel
                            )
                            MainTab.WORK -> WorkTabNavHost(
                                navController = navController,
                                viewModel = viewModel
                            )
                            MainTab.METERS -> MetersTabNavHost(
                                navController = navController,
                                viewModel = viewModel
                            )
                            MainTab.SETTINGS -> SettingsTabNavHost(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
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

// NavHost для вкладки Камера
@Composable
fun CameraTabNavHost(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoute.METER_SCAN.route
    ) {
        register(
            navController = navController,
            featureApi = viewModel.meterScanFeatureApi,
        )
    }
}

// NavHost для вкладки Работа
@Composable
fun WorkTabNavHost(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        startDestination = MainTab.WORK.route
    ) {
        register(
            navController = navController,
            featureApi = viewModel.workFeatureApi,
        )
    }
}

// NavHost для вкладки Счетчики
@Composable
fun MetersTabNavHost(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoute.METERS_LIST.route
    ) {
        register(
            navController = navController,
            featureApi = viewModel.metersFeatureApi,
        )
    }
}

// NavHost для вкладки Настройки
@Composable
fun SettingsTabNavHost(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoute.SETTINGS.route
    ) {
        register(
            navController = navController,
            featureApi = viewModel.settingsFeatureApi,
        )
    }
}
