package com.gabbasov.meterscan.di

import com.gabbasov.meterscan.presentation.MainActivityViewModel
import com.gabbasov.meterscan.repository.ScanSettingsRepository
import com.gabbasov.meterscan.scan.data.ScanSettingsRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val mainModule = module {
    viewModel { MainActivityViewModel(get(), get(), get(), get(), get(), get()) }
    singleOf(::ScanSettingsRepositoryImpl) bind ScanSettingsRepository::class
}
