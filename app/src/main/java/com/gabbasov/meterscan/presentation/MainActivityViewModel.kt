package com.gabbasov.meterscan.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabbasov.meterscan.repository.AuthRepository
import com.gabbasov.meterscan.features.MainScreenFeatureApi
import com.gabbasov.meterscan.features.MetersFeatureApi
import com.gabbasov.meterscan.features.SignInFeatureApi
import com.gabbasov.meterscan.features.SignUpFeatureApi
import com.gabbasov.meterscan.base.Resource
import com.gabbasov.meterscan.database.mock.MockDataProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainActivityViewModel(
    private val mockDataProvider: MockDataProvider,
    authRepository: AuthRepository,
    val signUpApi: SignUpFeatureApi,
    val signInApi: SignInFeatureApi,
    val metersFeatureApi: MetersFeatureApi,
    val mainScreenApi: MainScreenFeatureApi,
) : ViewModel() {

    val isAuthorized = authRepository.getCurrentUser()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
        .map {
            it is Resource.Success && it.data != null
        }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            mockDataProvider.populateMockDataIfNeeded()
        }
    }
}

