package com.gabbasov.meterscan.features

import com.gabbasov.meterscan.FeatureApi

interface SignUpFeatureApi : FeatureApi {
    fun signUpRoute(): String
}
