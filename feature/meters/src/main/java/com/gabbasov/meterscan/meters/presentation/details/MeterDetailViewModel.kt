package com.gabbasov.meterscan.meters.presentation.details

import androidx.lifecycle.viewModelScope
import com.gabbasov.meterscan.meters.domain.MetersRepository
import com.gabbasov.meterscan.network.Resource
import com.gabbasov.meterscan.ui.BaseViewModel
import com.gabbasov.meterscan.ui.Text
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class MeterDetailViewModel(
    private val metersRepository: MetersRepository
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
            is MeterDetailAction.AddReading -> Unit // Обработка в координаторе
            is MeterDetailAction.EditMeter -> Unit // Обработка в координаторе
            is MeterDetailAction.DeleteMeter -> deleteMeter()
            is MeterDetailAction.NavigateBack -> Unit // Обработка в координаторе
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

    private fun deleteMeter() = viewModelScope.launch {
        val meterId = state.meter?.id ?: return@launch
        state = state.copy(isLoading = true)

        when (val result = metersRepository.deleteMeter(meterId)) {
            is Resource.Success -> {
                // Обработка успешного удаления выполняется в координаторе
                state = state.copy(isLoading = false)
            }

            is Resource.Error -> {
                state = state.copy(
                    isLoading = false,
                    error = Text.RawString(result.exception.message ?: "Ошибка удаления счетчика")
                )
            }
        }
    }
}
