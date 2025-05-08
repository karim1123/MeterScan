package com.gabbasov.meterscan.main.presentation

import androidx.lifecycle.ViewModel
import com.gabbasov.meterscan.features.MetersFeatureApi
import com.gabbasov.meterscan.features.SettingsFeatureApi

class MainViewModel(
    val metersFeatureApi: MetersFeatureApi,
    val settingsFeatureApi: SettingsFeatureApi,
) : ViewModel()

