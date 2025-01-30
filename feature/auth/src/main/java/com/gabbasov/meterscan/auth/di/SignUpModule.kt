package com.gabbasov.meterscan.auth.di

import com.gabbasov.meterscan.auth.data.repository.AuthRepositoryImpl
import com.gabbasov.meterscan.auth.data.repository.SignInUseCase
import com.gabbasov.meterscan.auth.data.repository.SignUpUseCase
import com.gabbasov.meterscan.auth.domain.AuthRepository
import com.gabbasov.meterscan.auth.presentation.signin.SignInViewModel
import com.gabbasov.meterscan.auth.presentation.signin.navigation.SignInNavigation
import com.gabbasov.meterscan.auth.presentation.signup.SignUpViewModel
import com.gabbasov.meterscan.auth.presentation.signup.navigation.SignUpNavigation
import com.gabbasov.meterscan.features.SignInFeatureApi
import com.gabbasov.meterscan.features.SignUpFeatureApi
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val signUpFeatureMode = module {
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
    singleOf(::SignUpNavigation) bind SignUpFeatureApi::class
    singleOf(::SignInNavigation) bind SignInFeatureApi::class
    single { SignUpUseCase(get()) }
    single { SignInUseCase(get()) }
    viewModel { SignUpViewModel(get()) }
    viewModel { SignInViewModel(get(), get()) }
}
