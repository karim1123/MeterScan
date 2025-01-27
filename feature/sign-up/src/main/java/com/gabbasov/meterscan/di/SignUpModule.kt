package com.gabbasov.meterscan.di

import com.gabbasov.meterscan.data.repository.SignUpRepository
import com.gabbasov.meterscan.data.repository.SignUpRepositoryImpl
import com.gabbasov.meterscan.presentation.SignUpViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val signUpFeatureMode =
    module {
        singleOf(::SignUpRepositoryImpl) bind SignUpRepository::class
        viewModel { SignUpViewModel(get()) }
    }
