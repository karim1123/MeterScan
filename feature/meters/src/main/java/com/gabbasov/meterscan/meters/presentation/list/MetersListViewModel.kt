package com.gabbasov.meterscan.meters.presentation.list

import androidx.lifecycle.viewModelScope
import com.gabbasov.meterscan.repository.MetersRepository
import com.gabbasov.meterscan.base.Resource
import com.gabbasov.meterscan.ui.BaseViewModel
import com.gabbasov.meterscan.ui.Text
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class MetersListViewModel(
    private val metersRepository: MetersRepository
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(MetersListState())
    val uiState = _uiState.asStateFlow()

    private var state: MetersListState
        get() = _uiState.value
        set(value) {
            _uiState.update { value }
        }

    init {
        execute(MetersListAction.LoadMeters)
    }

    fun execute(action: MetersListAction) {
        logAction(action)
        when (action) {
            is MetersListAction.LoadMeters -> loadMeters()
            is MetersListAction.MeterSelected -> Unit // Обработка в координаторе
            is MetersListAction.AddNewMeter -> Unit // Обработка в координаторе
        }
    }

    private fun loadMeters() = viewModelScope.launch {
        state = state.copy(isLoading = true)

        metersRepository.getAllMeters().collect { result ->
            state = when (result) {
                is Resource.Success -> {
                    state.copy(
                        isLoading = false,
                        meters = result.data.toImmutableList(),
                        error = null
                    )
                }

                is Resource.Error -> {
                    state.copy(
                        isLoading = false,
                        error = Text.RawString(
                            result.exception.message ?: "Ошибка загрузки счетчиков"
                        )
                    )
                }
            }
        }
    }
}
