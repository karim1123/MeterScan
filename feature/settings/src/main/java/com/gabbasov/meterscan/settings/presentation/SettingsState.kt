package com.gabbasov.meterscan.settings.presentation

import androidx.compose.runtime.Stable
import com.gabbasov.meterscan.domain.BaseAction
import com.gabbasov.meterscan.domain.BaseState
import com.gabbasov.meterscan.model.navigator.NavigatorType

@Stable
data class SettingsState(
    val userEmail: String? = null,
    val useCameraMode: Boolean = false,
    val navigatorType: NavigatorType = NavigatorType.GOOGLE_MAPS,
    val appVersion: String = "1.0.0",
    override val isLoading: Boolean = false,
    override val error: com.gabbasov.meterscan.ui.Text? = null
) : BaseState()

sealed interface SettingsAction : BaseAction {
    data class SetCameraMode(val enabled: Boolean) : SettingsAction
    data class SetNavigatorType(val type: NavigatorType) : SettingsAction
    data object SignOut : SettingsAction
}
