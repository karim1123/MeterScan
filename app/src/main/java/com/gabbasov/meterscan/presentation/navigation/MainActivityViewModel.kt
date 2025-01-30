package com.gabbasov.meterscan.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabbasov.meterscan.auth.domain.AuthRepository
import com.gabbasov.meterscan.features.SignInFeatureApi
import com.gabbasov.meterscan.features.SignUpFeatureApi
import com.gabbasov.meterscan.network.Resource
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainActivityViewModel(
    private val authRepository: AuthRepository,
    val signUpApi: SignUpFeatureApi,
    val signInApi: SignInFeatureApi,
) : ViewModel() {

    val isAuthorized = authRepository.getCurrentUser()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
        .map {
            it is Resource.Success && it.data != null
        }
}

