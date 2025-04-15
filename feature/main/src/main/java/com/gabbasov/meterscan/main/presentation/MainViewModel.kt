package com.gabbasov.meterscan.main.presentation

import androidx.lifecycle.ViewModel
import com.gabbasov.meterscan.features.MetersFeatureApi

class MainViewModel(
    val metersFeatureApi: MetersFeatureApi,
) : ViewModel()

