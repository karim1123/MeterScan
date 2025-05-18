package com.gabbasov.meterscan.scan.presentation

import androidx.compose.runtime.Stable
import com.gabbasov.meterscan.domain.base.BaseAction
import com.gabbasov.meterscan.domain.base.BaseState
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.scan.domain.DigitBox
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Stable
data class MeterScanState(
    val detectedDigits: ImmutableList<DigitBox> = persistentListOf(),
    val meterReading: String = "",
    val defaultPickerCount: Int = 4,
    val showBottomSheet: Boolean = false,
    val showErrorDialog: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val showLowerValueWarning: Boolean = false,
    val newReading: String = "",
    val goBackAfterSave: Boolean = false,
    val navigateBack: Boolean = false,
    val isScanning: Boolean = false,
    val recognitionProgress: Float = 0f,
    val stabilityScore: Float = 0f,
    val flashlightEnabled: Boolean = false,
    val cameraRotation: Int = 0,
    val isPaused: Boolean = true,
    val selectedMeter: Meter? = null,
    val showMeterSelection: Boolean = false,
    override val isLoading: Boolean = false,
    override val error: com.gabbasov.meterscan.ui.Text? = null
) : BaseState()

sealed interface MeterScanAction : BaseAction {
    data class DetectDigits(val digitBoxes: List<DigitBox>) : MeterScanAction
    data class UpdateReading(val reading: String) : MeterScanAction
    data class SaveReading(val reading: String, val goBack: Boolean = false) : MeterScanAction
    data class SetGoBackAfterSave(val goBack: Boolean) : MeterScanAction
    data object RetryScanning : MeterScanAction
    data object EnterManually : MeterScanAction
    data object DismissBottomSheet : MeterScanAction
    data object DismissErrorDialog : MeterScanAction
    data object ToggleFlashlight : MeterScanAction
    data object RotateCamera : MeterScanAction
    data object TogglePause : MeterScanAction
    data object ConfirmLowerValue : MeterScanAction
    data object DismissLowerValueWarning : MeterScanAction
    data object NavigationHandled : MeterScanAction
    data class LoadMeter(val meterId: String) : MeterScanAction
    data class SelectMeter(val meter: Meter) : MeterScanAction
    data object ShowMeterSelection : MeterScanAction
    data object HideMeterSelection : MeterScanAction
    data object NavigateToMetersList : MeterScanAction
}
