package com.gabbasov.meterscan.meters.presentation.list

import androidx.compose.runtime.Stable
import com.gabbasov.meterscan.domain.BaseAction
import com.gabbasov.meterscan.domain.BaseState
import com.gabbasov.meterscan.meters.domain.Meter
import com.gabbasov.meterscan.ui.Text

@Stable
internal data class MetersListState(
    val meters: List<Meter> = emptyList(),
    override val isLoading: Boolean = false,
    override val error: Text? = null,
) : BaseState()

internal sealed interface MetersListAction : BaseAction {
    data object LoadMeters : MetersListAction
    data class MeterSelected(val meterId: String) : MetersListAction
    data object AddNewMeter : MetersListAction
}
