package com.gabbasov.meterscan.repository

import com.gabbasov.meterscan.model.navigator.NavigatorType

interface SettingsRepository {
    suspend fun getCameraMode(): Boolean
    suspend fun saveCameraMode(enabled: Boolean)

    suspend fun getNavigatorType(): NavigatorType
    suspend fun saveNavigatorType(type: NavigatorType)

    fun getAppVersion(): String
}