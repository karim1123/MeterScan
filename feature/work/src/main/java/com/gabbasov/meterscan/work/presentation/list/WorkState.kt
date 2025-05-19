package com.gabbasov.meterscan.work.presentation.list

import androidx.compose.runtime.Stable
import com.gabbasov.meterscan.common.domain.base.BaseAction
import com.gabbasov.meterscan.common.domain.base.BaseState
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.model.meter.MeterState
import com.gabbasov.meterscan.model.navigator.NavigatorType
import com.gabbasov.meterscan.common.ui.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Stable
internal data class WorkState(
    val meters: ImmutableList<Meter> = persistentListOf(),
    val filteredMeters: ImmutableList<Meter> = persistentListOf(),
    val searchQuery: String = "",
    val navigateToScan: String? = null,
    val showReadingDialog: Boolean = false,
    val newReading: String = "",
    val showLowerValueWarning: Boolean = false,
    val selectedMeterId: String? = null,
    val userLocation: Pair<Double, Double>? = null, // Местоположение пользователя
    val navigatorType: NavigatorType = NavigatorType.SYSTEM_DEFAULT, // Тип навигатора из настроек
    val selectedTabIndex: Int = 0, // Индекс выбранной вкладки (0 - список, 1 - карта)
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
    data class SaveReading(val reading: String) : WorkAction
    data object ConfirmLowerValue : WorkAction
    data object DismissReadingDialog : WorkAction
    data object DismissLowerValueWarning : WorkAction
    data class ShowReadingDialog(val meterId: String) : WorkAction

    // Новые действия для работы с картой
    data class SetUserLocation(val location: Pair<Double, Double>) : WorkAction
    data object LoadNavigatorType : WorkAction
    data class BuildRoute(val meter: Meter) : WorkAction
    data class SetSelectedTabIndex(val index: Int) : WorkAction
}
