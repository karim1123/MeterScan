package com.gabbasov.meterscan.work.di

import com.gabbasov.meterscan.features.WorkFeatureApi
import com.gabbasov.meterscan.work.presentation.list.WorkViewModel
import com.gabbasov.meterscan.work.presentation.navigation.WorkNavigation
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val workFeatureModule = module {
    singleOf(::WorkNavigation) bind WorkFeatureApi::class
    viewModel { WorkViewModel(get()) }
}
