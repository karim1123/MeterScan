package com.gabbasov.meterscan.features

import com.gabbasov.meterscan.FeatureApi

interface SignInFeatureApi : FeatureApi {
    fun signInRoute(): String
}
