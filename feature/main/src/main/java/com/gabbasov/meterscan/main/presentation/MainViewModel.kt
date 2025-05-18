package com.gabbasov.meterscan.main.presentation

import androidx.lifecycle.ViewModel
import com.gabbasov.meterscan.features.MeterScanFeatureApi
import com.gabbasov.meterscan.features.MetersFeatureApi
import com.gabbasov.meterscan.features.SettingsFeatureApi
import com.gabbasov.meterscan.features.WorkFeatureApi

class MainViewModel(
    val metersFeatureApi: MetersFeatureApi,
    val settingsFeatureApi: SettingsFeatureApi,
    val meterScanFeatureApi: MeterScanFeatureApi,
    val workFeatureApi: WorkFeatureApi,
) : ViewModel()

