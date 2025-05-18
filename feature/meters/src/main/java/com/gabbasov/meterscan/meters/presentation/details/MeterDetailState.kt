package com.gabbasov.meterscan.meters.presentation.details

import androidx.compose.runtime.Stable
import com.gabbasov.meterscan.domain.base.BaseAction
import com.gabbasov.meterscan.domain.base.BaseState
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.ui.Text

@Stable
internal data class MeterDetailState(
    val meter: Meter? = null,
    override val isLoading: Boolean = false,
    override val error: Text? = null,
) : BaseState()

internal sealed interface MeterDetailAction : BaseAction {
    data class LoadMeter(val meterId: String) : MeterDetailAction
    data object AddReading : MeterDetailAction
    data object EditMeter : MeterDetailAction
    data object DeleteMeter : MeterDetailAction
    data object NavigateBack : MeterDetailAction
}
