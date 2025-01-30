package com.gabbasov.meterscan.auth.di

import com.gabbasov.meterscan.auth.domain.SignUpRepository
import com.gabbasov.meterscan.auth.data.repository.SignUpRepositoryImpl
import com.gabbasov.meterscan.auth.data.repository.SignUpUseCase
import com.gabbasov.meterscan.features.SignUpFeatureApi
import com.gabbasov.meterscan.auth.presentation.signup.SignUpViewModel
import com.gabbasov.meterscan.auth.presentation.signup.navigation.SignUpNavigation
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val signUpFeatureMode = module {
    singleOf(::SignUpRepositoryImpl) bind SignUpRepository::class
    singleOf(::SignUpNavigation) bind SignUpFeatureApi::class
    single { SignUpUseCase(get()) }
    viewModel { SignUpViewModel(get()) }
}
