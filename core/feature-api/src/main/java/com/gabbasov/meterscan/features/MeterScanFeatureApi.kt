package com.gabbasov.meterscan.features

import com.gabbasov.meterscan.FeatureApi

interface MeterScanFeatureApi: FeatureApi {

    fun meterScanRoute(): String
}
