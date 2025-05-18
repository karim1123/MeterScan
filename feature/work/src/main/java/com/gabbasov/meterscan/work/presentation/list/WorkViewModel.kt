package com.gabbasov.meterscan.work.presentation.list

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.gabbasov.meterscan.repository.MetersRepository
import com.gabbasov.meterscan.base.Resource
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.model.meter.MeterState
import com.gabbasov.meterscan.ui.BaseViewModel
import com.gabbasov.meterscan.ui.Text
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class WorkViewModel(
    private val metersRepository: MetersRepository
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(WorkState())
    val uiState = _uiState.asStateFlow()

    private var state: WorkState
        get() = _uiState.value
        set(value) {
            _uiState.update { value }
        }

    init {
        execute(WorkAction.LoadMeters)
    }

    fun execute(action: WorkAction) {
        logAction(action)
        when (action) {
            is WorkAction.LoadMeters -> loadMeters()
            is WorkAction.MeterSelected -> Unit // Обработка в координаторе
            is WorkAction.TakeReading -> navigateToScan(action.meterId)
            is WorkAction.SearchMeters -> searchMeters(action.query)
            is WorkAction.NavigationHandled -> state = state.copy(navigateToScan = null)
            is WorkAction.UpdateMeterState -> updateMeterState(action.meterId, action.newState)
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
                        filteredMeters = filterMeters(result.data, state.searchQuery).toImmutableList(),
                        error = null
                    )
                }
                is Resource.Error -> {
                    Log.d("WorkViewModel", "result.exception: ${result.exception}")
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

    private fun searchMeters(query: String) {
        state = state.copy(
            searchQuery = query,
            filteredMeters = filterMeters(state.meters, query).toImmutableList()
        )
    }

    private fun filterMeters(meters: List<Meter>, query: String): List<Meter> {
        if (query.isBlank()) return meters

        val lowercaseQuery = query.lowercase()
        return meters.filter { meter ->
            meter.number.lowercase().contains(lowercaseQuery) ||
                    meter.address.getFullAddress().lowercase().contains(lowercaseQuery) ||
                    meter.owner.lowercase().contains(lowercaseQuery) ||
                    meter.type.name.lowercase().contains(lowercaseQuery)
        }
    }

    private fun navigateToScan(meterId: String) {
        state = state.copy(navigateToScan = meterId)
    }

    private fun updateMeterState(meterId: String, newState: MeterState) = viewModelScope.launch {
        val meter = state.meters.find { meter -> meter.id == meterId }

        if (meter != null) {
            val updatedMeter = meter.copy(state = newState)

            when (val result = metersRepository.updateMeter(updatedMeter)) {
                is Resource.Success -> {
                    if (newState == MeterState.SAVED_LOCALLY) {
                        simulateServerSubmission(meterId)
                    } else {
                        loadMeters()
                    }
                }
                is Resource.Error -> {
                    state = state.copy(
                        error = Text.RawString(
                            result.exception.message ?: "Ошибка обновления состояния счетчика"
                        )
                    )
                }
            }
        }
    }

    private fun simulateServerSubmission(meterId: String) = viewModelScope.launch {
        delay(1500)
        val meter = state.meters.find { it.id == meterId }

        if (meter != null) {
            val updatedMeter = meter.copy(state = MeterState.SUBMITTED_TO_SERVER)

            when (val result = metersRepository.updateMeter(updatedMeter)) {
                is Resource.Success -> loadMeters()
                is Resource.Error -> {
                    state = state.copy(
                        error = Text.RawString(
                            result.exception.message ?: "Ошибка отправки на сервер"
                        )
                    )
                }
            }
        }
    }}
