package com.gabbasov.meterscan.meters.presentation.list

import androidx.compose.runtime.Stable
import com.gabbasov.meterscan.domain.BaseAction
import com.gabbasov.meterscan.domain.BaseState
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.ui.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Stable
internal data class MetersListState(
    val meters: ImmutableList<Meter> = persistentListOf(),
    override val isLoading: Boolean = false,
    override val error: Text? = null,
) : BaseState()

internal sealed interface MetersListAction : BaseAction {
    data object LoadMeters : MetersListAction
    data class MeterSelected(val meterId: String) : MetersListAction
    data object AddNewMeter : MetersListAction
}
