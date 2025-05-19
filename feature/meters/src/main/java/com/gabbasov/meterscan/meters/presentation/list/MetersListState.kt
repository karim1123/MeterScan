package com.gabbasov.meterscan.meters.presentation.list

import androidx.compose.runtime.Stable
import com.gabbasov.meterscan.common.domain.base.BaseAction
import com.gabbasov.meterscan.common.domain.base.BaseState
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.common.ui.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Stable
internal data class MetersListState(
    val meters: ImmutableList<Meter> = persistentListOf(),
    val filteredMeters: ImmutableList<Meter> = persistentListOf(),
    val searchQuery: String = "",
    override val isLoading: Boolean = false,
    override val error: Text? = null,
) : BaseState()

internal sealed interface MetersListAction : BaseAction {
    data object LoadMeters : MetersListAction
    data class MeterSelected(val meterId: String) : MetersListAction
    data class SearchMeters(val query: String) : MetersListAction
    data object AddNewMeter : MetersListAction
}
