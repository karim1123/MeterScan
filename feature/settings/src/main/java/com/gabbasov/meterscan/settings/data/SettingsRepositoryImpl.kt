package com.gabbasov.meterscan.settings.data

import android.content.Context
import android.content.SharedPreferences
import com.gabbasov.meterscan.model.navigator.NavigatorType
import com.gabbasov.meterscan.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl(
    private val context: Context
) : SettingsRepository {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)

    override suspend fun getCameraMode(): Boolean = withContext(Dispatchers.IO) {
        prefs.getBoolean(KEY_CAMERA_MODE, true)
    }

    override suspend fun saveCameraMode(enabled: Boolean) = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(KEY_CAMERA_MODE, enabled).apply()
    }

    override suspend fun getNavigatorType(): NavigatorType = withContext(Dispatchers.IO) {
        val typeOrdinal = prefs.getInt(KEY_NAVIGATOR_TYPE, NavigatorType.SYSTEM_DEFAULT.ordinal)
        NavigatorType.entries[typeOrdinal]
    }

    override suspend fun saveNavigatorType(type: NavigatorType) = withContext(Dispatchers.IO) {
        prefs.edit().putInt(KEY_NAVIGATOR_TYPE, type.ordinal).apply()
    }

    override fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.versionCode})"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    companion object {
        private const val SETTINGS_PREFS = "settings_preferences"
        private const val KEY_CAMERA_MODE = "camera_mode"
        private const val KEY_NAVIGATOR_TYPE = "navigator_type"
    }
}