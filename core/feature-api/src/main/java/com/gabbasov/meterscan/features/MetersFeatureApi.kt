package com.gabbasov.meterscan.features

import com.gabbasov.meterscan.FeatureApi

interface MetersFeatureApi : FeatureApi {
    fun meterListRoute(): String
}
