package com.gabbasov.meterscan.presentation.navigation

import androidx.lifecycle.ViewModel
import com.gabbasov.meterscan.features.SignUpFeatureApi

class MainActivityViewModel(
    val signUpApi: SignUpFeatureApi,
) : ViewModel()
