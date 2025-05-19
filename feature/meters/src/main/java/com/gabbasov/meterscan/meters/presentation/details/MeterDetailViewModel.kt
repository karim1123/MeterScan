package com.gabbasov.meterscan.meters.presentation.details

import androidx.lifecycle.viewModelScope
import com.gabbasov.meterscan.base.Resource
import com.gabbasov.meterscan.repository.MetersRepository
import com.gabbasov.meterscan.repository.SettingsRepository
import com.gabbasov.meterscan.common.ui.BaseViewModel
import com.gabbasov.meterscan.common.ui.Text
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class MeterDetailViewModel(
    private val metersRepository: MetersRepository,
    private val settingsRepository: SettingsRepository,
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(MeterDetailState())
    val uiState = _uiState.asStateFlow()

    private var state: MeterDetailState
        get() = _uiState.value
        set(value) {
            _uiState.update { value }
        }

    fun execute(action: MeterDetailAction) {
        logAction(action)
        when (action) {
            is MeterDetailAction.LoadMeter -> loadMeter(action.meterId)
            is MeterDetailAction.AddReading -> handleAddReading()
            is MeterDetailAction.NavigateBack -> Unit // Обработка в координаторе
            is MeterDetailAction.SaveReading -> checkAndSaveReading(action.reading)
            is MeterDetailAction.ConfirmLowerValue -> forceAddReading()
            is MeterDetailAction.DismissReadingDialog -> state =
                state.copy(showReadingDialog = false)

            is MeterDetailAction.DismissLowerValueWarning -> state =
                state.copy(showLowerValueWarning = false)

            is MeterDetailAction.NavigationHandled -> state = state.copy(navigateToScan = null)
        }
    }

    private fun loadMeter(meterId: String) = viewModelScope.launch {
        state = state.copy(isLoading = true)

        metersRepository.getMeterById(meterId).collect { result ->
            when (result) {
                is Resource.Success -> {
                    state = state.copy(
                        isLoading = false,
                        meter = result.data,
                        error = null
                    )
                }

                is Resource.Error -> {
                    state = state.copy(
                        isLoading = false,
                        error = Text.RawString(
                            result.exception.message ?: "Ошибка загрузки счетчика"
                        )
                    )
                }
            }
        }
    }

    private fun handleAddReading() = viewModelScope.launch {
        val currentMeter = state.meter ?: return@launch
        val useCameraMode = settingsRepository.getCameraMode()

        state = if (useCameraMode) {
            // Откроем экран сканирования с нужным ID
            state.copy(navigateToScan = currentMeter.id)
        } else {
            // Покажем диалог ручного ввода
            state.copy(showReadingDialog = true)
        }
    }

    private fun checkAndSaveReading(reading: String) {
        val readingValue = reading.toDoubleOrNull() ?: return
        val currentMeter = state.meter ?: return

        // Проверяем, что новые показания не меньше последних
        val lastReading = currentMeter.readings.maxByOrNull { it.date }?.value ?: 0.0

        if (readingValue < lastReading) {
            state = state.copy(
                showLowerValueWarning = true,
                newReading = reading
            )
        } else {
            addReadingToMeter(readingValue)
        }
    }

    private fun forceAddReading() {
        val readingValue = state.newReading.toDoubleOrNull() ?: return
        addReadingToMeter(readingValue)
        state = state.copy(showLowerValueWarning = false)
    }

    private fun addReadingToMeter(reading: Double) = viewModelScope.launch {
        val meterId = state.meter?.id ?: return@launch
        state = state.copy(isLoading = true, showReadingDialog = false)

        when (val result = metersRepository.addReading(meterId, reading)) {
            is Resource.Success -> {
                loadMeter(meterId) // Перезагружаем данные счетчика
            }

            is Resource.Error -> {
                state = state.copy(
                    isLoading = false,
                    error = Text.RawString(
                        result.exception.message ?: "Ошибка сохранения показаний"
                    )
                )
            }
        }
    }
}