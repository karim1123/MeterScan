package com.gabbasov.meterscan.scan.di

import com.gabbasov.meterscan.features.MeterScanFeatureApi
import com.gabbasov.meterscan.scan.presentation.MeterScanViewModel
import com.gabbasov.meterscan.scan.presentation.navigation.MeterScanNavigation
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val scanModule = module {
    singleOf(::MeterScanNavigation) bind MeterScanFeatureApi::class
    viewModel { MeterScanViewModel(get()) }
}
