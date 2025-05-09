package com.gabbasov.meterscan.scan.presentation

import androidx.lifecycle.viewModelScope
import com.gabbasov.meterscan.base.Resource
import com.gabbasov.meterscan.scan.domain.DigitBox
import com.gabbasov.meterscan.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MeterScanViewModel : BaseViewModel() {
    private val _uiState = MutableStateFlow(MeterScanState())
    val uiState = _uiState.asStateFlow()

    private var state: MeterScanState
        get() = _uiState.value
        set(value) {
            _uiState.update { value }
        }

    fun execute(action: MeterScanAction) {
        logAction(action)
        when (action) {
            is MeterScanAction.DetectDigits -> processDetectedDigits(action.digitBoxes)
            is MeterScanAction.UpdateReading -> updateMeterReading(action.reading)
            is MeterScanAction.SaveReading -> saveReading(action.reading)
            is MeterScanAction.RetryScanning -> retryScanning()
            is MeterScanAction.EnterManually -> showEditDialog()
            is MeterScanAction.DismissBottomSheet -> dismissBottomSheet()
            is MeterScanAction.DismissErrorDialog -> dismissErrorDialog()
        }
    }

    private fun processDetectedDigits(digitBoxes: List<DigitBox>) {
        if (digitBoxes.isEmpty()) {
            state = state.copy(
                showErrorDialog = true,
                isScanning = false
            )
            return
        }

        // Сортируем цифры по X-координате для получения правильного порядка
        val sortedDigits = digitBoxes.sortedBy { it.cx }

        // Формируем показания счетчика из распознанных цифр
        val reading = sortedDigits.joinToString("") { it.digit }

        state = state.copy(
            detectedDigits = sortedDigits,
            meterReading = reading,
            showBottomSheet = true,
            isScanning = false
        )
    }

    private fun updateMeterReading(reading: String) {
        state = state.copy(
            meterReading = reading
        )
    }

    private fun saveReading(reading: String) = viewModelScope.launch {
        state = state.copy(isLoading = true)

        // Здесь должен быть вызов репозитория для сохранения показаний
        // Например: val result = meterRepository.saveReading(reading)

        // Имитация сохранения
        val result = Resource.Success(Unit)

        when (result) {
            is Resource.Success -> {
                state = state.copy(
                    isLoading = false,
                    showBottomSheet = false,
                    showSuccessMessage = true
                )
            }
            /*is Resource.Error -> {
                state = state.copy(
                    isLoading = false,
                    error = Text.RawString(result.exception.message ?: "Ошибка сохранения показаний")
                )
            }*/
        }
    }

    private fun retryScanning() {
        state = state.copy(
            isScanning = true,
            showErrorDialog = false
        )
    }

    private fun showEditDialog() {
        state = state.copy(
            showBottomSheet = true,
            showErrorDialog = false
        )
    }

    private fun dismissBottomSheet() {
        state = state.copy(
            showBottomSheet = false
        )
    }

    private fun dismissErrorDialog() {
        state = state.copy(
            showErrorDialog = false
        )
    }
}