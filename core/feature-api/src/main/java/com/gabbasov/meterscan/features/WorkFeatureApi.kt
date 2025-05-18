package com.gabbasov.meterscan.features

import com.gabbasov.meterscan.FeatureApi

interface WorkFeatureApi : FeatureApi {
    fun meterListRoute(): String
}
