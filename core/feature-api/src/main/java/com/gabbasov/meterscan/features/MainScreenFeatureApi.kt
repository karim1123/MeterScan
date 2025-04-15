package com.gabbasov.meterscan.features

import com.gabbasov.meterscan.FeatureApi

interface MainScreenFeatureApi : FeatureApi {
    fun mainScreenRoute(): String
}