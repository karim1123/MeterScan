package com.gabbasov.meterscan.meters.presentation.details

import androidx.compose.runtime.Stable
import com.gabbasov.meterscan.domain.base.BaseAction
import com.gabbasov.meterscan.domain.base.BaseState
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.ui.Text

@Stable
internal data class MeterDetailState(
    val meter: Meter? = null,
    val navigateToScan: String? = null,
    val showReadingDialog: Boolean = false,
    val newReading: String = "",
    val showLowerValueWarning: Boolean = false,
    override val isLoading: Boolean = false,
    override val error: Text? = null,
) : BaseState()

internal sealed interface MeterDetailAction : BaseAction {
    data class LoadMeter(val meterId: String) : MeterDetailAction
    data object AddReading : MeterDetailAction
    data object NavigateBack : MeterDetailAction
    data class SaveReading(val reading: String) : MeterDetailAction
    data object ConfirmLowerValue : MeterDetailAction
    data object DismissReadingDialog : MeterDetailAction
    data object DismissLowerValueWarning : MeterDetailAction
    data object NavigationHandled : MeterDetailAction
}
