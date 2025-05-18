package com.gabbasov.meterscan.work.presentation.list

import androidx.compose.runtime.Stable
import com.gabbasov.meterscan.domain.base.BaseAction
import com.gabbasov.meterscan.domain.base.BaseState
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.ui.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Stable
internal data class WorkMetersListState(
    val meters: ImmutableList<Meter> = persistentListOf(),
    val filteredMeters: ImmutableList<Meter> = persistentListOf(),
    val searchQuery: String = "",
    override val isLoading: Boolean = false,
    override val error: Text? = null,
) : BaseState()

internal sealed interface WorkMetersListAction : BaseAction {
    data object LoadMeters : WorkMetersListAction
    data class MeterSelected(val meterId: String) : WorkMetersListAction
    data class SearchMeters(val query: String) : WorkMetersListAction
    data object AddNewMeter : WorkMetersListAction
}
