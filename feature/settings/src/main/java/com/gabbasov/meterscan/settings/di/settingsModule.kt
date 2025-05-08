package com.gabbasov.meterscan.settings.di

import com.gabbasov.meterscan.features.SettingsFeatureApi
import com.gabbasov.meterscan.repository.SettingsRepository
import com.gabbasov.meterscan.settings.data.SettingsRepositoryImpl
import com.gabbasov.meterscan.settings.presentation.SettingsViewModel
import com.gabbasov.meterscan.settings.presentation.navigation.SettingsNavigation
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val settingsModule = module {
    singleOf(::SettingsRepositoryImpl) bind SettingsRepository::class
    singleOf(::SettingsNavigation) bind SettingsFeatureApi::class
    viewModel { SettingsViewModel(get(), get(), get()) }
}
