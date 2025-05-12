package com.gabbasov.meterscan.scan.presentation

import androidx.compose.runtime.Stable
import com.gabbasov.meterscan.domain.BaseAction
import com.gabbasov.meterscan.domain.BaseState
import com.gabbasov.meterscan.scan.domain.DigitBox

@Stable
data class MeterScanState(
    val detectedDigits: List<DigitBox> = emptyList(),
    val meterReading: String = "",
    val showBottomSheet: Boolean = false,
    val showErrorDialog: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val isScanning: Boolean = true,
    val recognitionProgress: Float = 0f,
    val stabilityScore: Float = 0f,
    val flashlightEnabled: Boolean = false,
    val cameraRotation: Int = 0, // 0, 90, 180, 270
    override val isLoading: Boolean = false,
    override val error: com.gabbasov.meterscan.ui.Text? = null
) : BaseState()

sealed interface MeterScanAction : BaseAction {
    data class DetectDigits(val digitBoxes: List<DigitBox>) : MeterScanAction
    data class UpdateReading(val reading: String) : MeterScanAction
    data class SaveReading(val reading: String) : MeterScanAction
    data object RetryScanning : MeterScanAction
    data object EnterManually : MeterScanAction
    data object DismissBottomSheet : MeterScanAction
    data object DismissErrorDialog : MeterScanAction
    data object ToggleFlashlight  : MeterScanAction
    data object RotateCamera : MeterScanAction
}