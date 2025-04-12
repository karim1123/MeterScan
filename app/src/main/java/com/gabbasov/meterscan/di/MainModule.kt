package com.gabbasov.meterscan.di

import com.gabbasov.meterscan.presentation.MainActivityViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {
    viewModel { MainActivityViewModel(get(), get(), get(), get(), get()) }
}
