package com.gabbasov.meterscan.features

import com.gabbasov.meterscan.FeatureApi

interface SettingsFeatureApi : FeatureApi {
    fun settingsRoute(): String
}