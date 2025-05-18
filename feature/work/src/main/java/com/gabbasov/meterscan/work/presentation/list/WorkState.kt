package com.gabbasov.meterscan.work.presentation.list

import androidx.compose.runtime.Stable
import com.gabbasov.meterscan.domain.base.BaseAction
import com.gabbasov.meterscan.domain.base.BaseState
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.model.meter.MeterState
import com.gabbasov.meterscan.ui.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Stable
internal data class WorkState(
    val meters: ImmutableList<Meter> = persistentListOf(),
    val filteredMeters: ImmutableList<Meter> = persistentListOf(),
    val searchQuery: String = "",
    val navigateToScan: String? = null,
    override val isLoading: Boolean = false,
    override val error: Text? = null,
) : BaseState()

internal sealed interface WorkAction : BaseAction {
    data object LoadMeters : WorkAction
    data class MeterSelected(val meterId: String) : WorkAction
    data class SearchMeters(val query: String) : WorkAction
    data class TakeReading(val meterId: String) : WorkAction
    data object NavigationHandled : WorkAction
    data class UpdateMeterState(
        val meterId: String,
        val newState: MeterState
    ) : WorkAction
}
