package com.gabbasov.meterscan.main.di

import com.gabbasov.meterscan.features.MainScreenFeatureApi
import com.gabbasov.meterscan.main.presentation.MainViewModel
import com.gabbasov.meterscan.main.presentation.navigation.MainScreenNavigation
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val mainFeatureModule = module {
    singleOf(::MainScreenNavigation) bind MainScreenFeatureApi::class
    viewModel { MainViewModel(get()) }

}
